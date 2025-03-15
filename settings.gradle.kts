pluginManagement {
    repositories {
        google() // Required for Firebase
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
    plugins {
        id("com.google.gms.google-services") version "4.4.0"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // Required for Firebase
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "habitflow"
include(":app")