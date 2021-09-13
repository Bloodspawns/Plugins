allprojects {
    group = "com.injector"
    version = "1.0.0"
}

buildscript {
    repositories{
        gradlePluginPortal()
        mavenLocal()
    }
}

subprojects{

    repositories {
        jcenter()
        maven(url = "https://mvnrepository.com/artifact")
        maven(url = "https://repo.runelite.net")
        maven(url = "https://jitpack.io")

        mavenLocal()
    }

    apply<JavaLibraryPlugin>()

    tasks {

        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }
    }
}