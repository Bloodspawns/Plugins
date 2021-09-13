package net.runelite.client.plugins.blexternalmanager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.RuneLite;
import net.runelite.client.util.ImageUtil;

import javax.swing.ImageIcon;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public enum Controls
{
	RUNELITE("Open '.runelite'")
			{
				@Override
				public ImageIcon getIcon()
				{
					return remap(ImageUtil.loadImageResource(BLExternalManager.class, "folder.png"), Color.ORANGE);
				}

				@Override
				public MouseAdapter getMouseAdapter()
				{
					return new MouseAdapter()
					{
						@Override
						public void mousePressed(MouseEvent e)
						{
							try
							{
								Desktop.getDesktop().open(RuneLite.RUNELITE_DIR);
							}
							catch (IOException ignored) {}
						}
					};
				}
			},
	BEXTERNALS("Open 'bexternalplugins'")
			{
				@Override
				public ImageIcon getIcon()
				{
					return remap(ImageUtil.loadImageResource(BLExternalManager.class, "folder.png"), new Color(114, 188, 212));
				}

				@Override
				public MouseAdapter getMouseAdapter()
				{
					return new MouseAdapter()
					{
						@Override
						public void mousePressed(MouseEvent e)
						{
							try
							{
								Desktop.getDesktop().open(new File(RuneLite.RUNELITE_DIR, "bexternalplugins"));
							}
							catch (IOException ignored) {}
						}
					};
				}
			},
	BREPO("Open 'bpluginrepository'")
			{
				@Override
				public ImageIcon getIcon()
				{
					return remap(ImageUtil.loadImageResource(BLExternalManager.class, "folder.png"), Color.GRAY);
				}

				@Override
				public MouseAdapter getMouseAdapter()
				{
					return new MouseAdapter()
					{
						@Override
						public void mousePressed(MouseEvent e)
						{
							try
							{
								Desktop.getDesktop().open(new File(RuneLite.RUNELITE_DIR, "bpluginrepository"));
							}
							catch (IOException ignored) {}
						}
					};
				}
			},
	POWER("Disable all BlueLite Externals")
			{
				@Override
				public ImageIcon getIcon()
				{
					return new ImageIcon(ImageUtil.loadImageResource(BLExternalManager.class, "power.png"));
				}

				@Override
				public MouseAdapter getMouseAdapter()
				{
					return null;
				}
			};

	@Getter private final String tooltip;
	public abstract ImageIcon getIcon();
	public abstract MouseAdapter getMouseAdapter();

	private static ImageIcon remap(BufferedImage image, Color color)
	{
		BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics = img.createGraphics();
		graphics.drawImage(image, 0, 0, null);
		graphics.setColor(color);
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.75F));
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		graphics.dispose();

		return new ImageIcon(img);
	}
}
