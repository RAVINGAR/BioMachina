

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("idea")
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
    id("pmd")
    id("java-library")
}

group = "com.ravingarinc.biomachina"
version = "1.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    mavenLocal()
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")

    maven("https://repo.dmulloy2.net/repository/public/") {
        content {
            includeGroup("com.comphenix.protocol")
        }
    }

    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io")
}

val jsonSerialisationVersion = "1.5.0"
val jdbcVersion = "3.30.1"
val coroutinesVersion = "1.7.0-Beta"

val exposedVersion = "0.40.1"

dependencies {
    library(kotlin("stdlib"))
    library("org.jetbrains.kotlinx:kotlinx-serialization-json:$jsonSerialisationVersion")
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    library("org.jetbrains.exposed:exposed-core:$exposedVersion")
    library("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    library("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    library("org.xerial:sqlite-jdbc:$jdbcVersion")

    library("com.github.shynixn.mccoroutine", "mccoroutine-bukkit-api", "2.12.1")
    library("com.github.shynixn.mccoroutine", "mccoroutine-bukkit-core", "2.12.1")

    implementation("com.ravingarinc.api:common:1.4.0")
    implementation("com.ravingarinc.api:gui:1.4.0")
    implementation("com.ravingarinc.api:module:1.4.0")
    implementation("com.ravingarinc.api:version:1.4.0")
    compileOnly("org.jetbrains:annotations:23.1.0")

    //compileOnly("org.spigotmc:spigot:1.19.4-R0.1-SNAPSHOT:remapped-mojang")
    //compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0")
}

tasks {
    idea {
        module {
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }

    shadowJar {
        archiveBaseName.set("BioMachina")
        archiveClassifier.set("")
        archiveVersion.set("")
        relocate("com.ravingarinc.api", "com.ravingarinc.biomachina.libs.api")
    }

    /*
    spigotRemap {
        spigotVersion.set("1.19.4")
        sourceJarTask.set(shadowJar)
    }
    */

    artifacts {
        archives(shadowJar)
    }

    register<Copy>("copyToDev") {
        from(shadowJar)
        into(project.layout.projectDirectory.dir("../../Desktop/Programming/Servers/Latest/plugins"))
        //into "E:/Documents/Workspace/Servers/1.18.2-TEST/plugins/"
    }

    assemble {
        dependsOn(shadowJar)
        finalizedBy("copyToDev")
    }
    test {
        useJUnitPlatform()
        // Ensure testing is never "up-to-date" (in Gradle-speak), which means it can never be skipped,
        // as it would otherwise be.
        outputs.upToDateWhen { false }

        // Ensure we get all the useful test output.
        testLogging {
            events("failed", "passed", "skipped")
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

}

/*
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}*/
kotlin {
    jvmToolchain(17)
}

bukkit {
    name = "BioMachina"
    version = project.version as String
    description = "Vehicle management plugin for Biohazard"
    main = "com.ravingarinc.biomachina.BioMachina"

    // API version (should be set for 1.13+)
    apiVersion = "1.19"

    // Other possible properties from plugin.yml (optional)
    author = "RAVINGAR"
    depend = listOf("ProtocolLib")
    loadBefore = listOf("ProtocolLib")

    commands {
        register("biomachina") {
            aliases = listOf("bio", "bm")
            description = "Biomachina Admin Command"
            usage = "Unknown argument. Try /biomachina ?"
            permission = "biomachina.admin"
        }
    }
}
