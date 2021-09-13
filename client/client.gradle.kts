plugins {
    java
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

val patches = configurations.create("patches")
val client = configurations.create("client")
val plugins = configurations.create("plugins")
val externals = configurations.create("externals")
val baseRuneLite = configurations.create("baseRuneLite")
val baseBlueLite = configurations.create("baseBlueLite")
val repackedClient = configurations.create("repackedClient")

dependencies {
    client(Libraries.runelite_client)
    client(Libraries.runelite_jshell)
    baseBlueLite(fileTree(project.gradle.gradleUserHomeDir.parent + "/.runelite/bluerepo/"){
        include("*.jar") }.filter{ !isRLClient(it.name) && !isRLApi(it.name) && !isRLHttpApi(it.name)})
    baseRuneLite(files(client.files.filter { !isRLClient(it.name) }))
    repackedClient(files("build/libs/bclient-1.0.jar"))
    plugins(project(":plugins")) { isTransitive = false }
    externals(fileTree(project.gradle.gradleUserHomeDir.parent + "/.runelite/bexternalplugins/"){
        include("*.jar") })
    //patches(files("C:/Users/X/Documents/openosrs/injector/stuff/blue-patch.jar"))
}

fun isRLClient(name : String): Boolean {
    return Regex("client-([0-9]*\\.)*[0-9]*\\.jar").matches(name)
}

fun isRLApi(name : String): Boolean {
    return Regex("http-api-([0-9]*\\.)*[0-9]*\\.jar").matches(name)
}

fun isRLHttpApi(name : String): Boolean {
    return Regex("runelite-api-([0-9]*\\.)*[0-9]*\\.jar").matches(name)
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

//runclient.files.filter { it.name.startsWith("client") }.forEach{println(it.name)}


fun printClasspath(rl : Boolean) {
    fun names(set : Set<File>) : String {
        return set.joinToString(", ") { it.name }
    }
    println("--------------------------------------------------------")
    println("JAR files on the classpath in order of precedence are:")
    println("Plugins: " + names(plugins.files))
    println("Externals: " + names(externals.files))
    println("Repacked client: " + names(repackedClient.files))
    if (!rl) {
        println("BlueLite: " + names(baseBlueLite.files))
    }
    println("RuneLite: " + names(baseRuneLite.files))
    println("--------------------------------------------------------")
}

tasks {

    register<Jar>("repack") {
        doFirst {
            println("Repacking " + client.files.first { isRLClient(it.name) }.name + " into bclient-1.0.jar")
        }
        from(zipTree(client.first{ isRLClient(it.name) })) {
            exclude("META-INF/")
        }
        baseName = "bclient"
    }

    register<JavaExec>("Client.main()") {
//        dependsOn(":client:repack")
        doFirst {
            printClasspath(false)
        }
        group = "run"

        classpath = plugins.plus(patches).plus(externals).plus(repackedClient).plus(baseBlueLite).plus(baseRuneLite)
        main = "net.runelite.client.RuneLite"
        jvmArgs = listOf("-ea")
        args = listOf("--developer-mode", "--debug")
    }
}