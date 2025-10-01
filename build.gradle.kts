import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    java
    alias(libs.plugins.pluginYmlPaper)
}

group = "uk.co.notnull"
version = "1.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    mavenLocal()
}

dependencies {
    compileOnly(libs.paperApi)
}

paper {
    main = "xyz.derkades.spawnx.Main"
    apiVersion = libs.versions.paperApi.get().replace(Regex("\\-R\\d.\\d-SNAPSHOT"), "")
    authors = listOf("Jim (AnEnragedPigeon)", "Derkades")
    description = "Adds /spawn and teleports players to spawn on join."
}

tasks {
    compileJava {
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
        options.encoding = "UTF-8"
    }
}
