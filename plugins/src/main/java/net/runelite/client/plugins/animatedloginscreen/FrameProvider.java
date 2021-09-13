package net.runelite.client.plugins.animatedloginscreen;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class FrameProvider
{
	public static class SortedImage implements Comparable<SortedImage>
	{
		@Getter
		private final double timestamp;
		@Getter
		private final BufferedImage data;
		@Getter
		private final int frame;

		private SortedImage(PictureWithMetadata p, int frame)
		{
			data = AWTUtil.toBufferedImage(p.getPicture());
			timestamp = p.getTimestamp();
			this.frame = frame;
		}

		@Override
		public int compareTo(SortedImage o2)
		{
			return Double.compare(timestamp, o2.timestamp);
		}
	}

	private final int bufferedFrameCount = 9;
	private final PriorityBlockingQueue<SortedImage> queueCurrent = new PriorityBlockingQueue<>(bufferedFrameCount);
	private final PriorityBlockingQueue<SortedImage> queueNext = new PriorityBlockingQueue<>(bufferedFrameCount);
	private final AtomicBoolean use1 = new AtomicBoolean(true);
	private FrameGrab frameGrab;
	@Getter
	private boolean ready = false;
	private final AtomicInteger frame = new AtomicInteger(0);
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition canPreLoad = lock.newCondition();
	@Getter
	private int frameCount;

	public void loadVideo(File file)
	{
		if (ready)
		{
			throw new RuntimeException("Can't be loaded more than once");
		}

		try
		{
			frameGrab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
			frame.set(0);
			frameCount = frameGrab.getVideoTrack().getMeta().getTotalFrames();
			startLoadingLoop();
		}
		catch (IOException | NullPointerException | JCodecException e)
		{
			log.warn("Error loading video", e);
		}
	}

	public BufferedImage getFrame()
	{
		SortedImage image = null;
		lock.lock();
		try
		{
			try
			{
				image = getQueueCurrent().take();
			}
			catch (InterruptedException e)
			{
				log.warn("GetFrame Interrupted", e);
				return null;
			}
			frame.incrementAndGet();
			if (frame.get() >= frameCount && getQueueCurrent().isEmpty())
			{
				frame.set(0);
				negate(use1);
			}
			canPreLoad.signal();
		}
		finally
		{
			lock.unlock();
		}
		return image.getData();
	}

	private void negate(AtomicBoolean atomicBoolean)
	{
		boolean v;
		do {
			v=atomicBoolean.get();
		} while(!atomicBoolean.compareAndSet(v, !v));
	}

	private SortedImage getNextFrame()
	{
		PictureWithMetadata pictureWithMetadata;
		int frame = (int) frameGrab.getVideoTrack().getCurFrame();
		try
		{
			pictureWithMetadata = frameGrab.getNativeFrameWithMetadata();
			if (pictureWithMetadata == null)
			{
				frameGrab.getVideoTrack().gotoFrame(0);
				pictureWithMetadata = frameGrab.getNativeFrameWithMetadata();
			}
		}
		catch (IOException e)
		{
			log.warn("Error getting frame", e);
			return null;
		}

		return new SortedImage(pictureWithMetadata, frame);
	}

	private PriorityBlockingQueue<SortedImage> getQueueCurrent()
	{
		if (use1.get())
		{
			return queueCurrent;
		}
		return queueNext;
	}

	private PriorityBlockingQueue<SortedImage> getQueueNext()
	{
		if (use1.get())
		{
			return queueNext;
		}
		return queueCurrent;
	}

	private void startLoadingLoop()
	{
		// create buffer
		for (int i = 0; i < bufferedFrameCount; i++)
		{
			var sortedImage = getNextFrame();
			if (sortedImage == null)
			{
				continue;
			}
			queueCurrent.add(sortedImage);
		}

		if (queueCurrent.size() < bufferedFrameCount)
		{
			throw new RuntimeException("queue not the size of buffer after init");
		}
		ready = true;
		new Thread(() ->
		{
			while (true)
			{
				lock.lock();
				try
				{
					while ((queueCurrent.size() + queueNext.size()) >= bufferedFrameCount)
					{
						try
						{
							canPreLoad.await();
						}
						catch (InterruptedException e)
						{
							log.warn("Pre-fetching thread interrupted", e);
						}
					}

					var sortedImage = getNextFrame();
					if (sortedImage == null)
					{
						continue;
					}

					if (sortedImage.getFrame() < frame.get())
					{
						getQueueNext().add(sortedImage);
					}
					else
					{
						getQueueCurrent().add(sortedImage);
					}

				}
				finally
				{
					lock.unlock();
				}
			}
		}).start();
	}
}
