
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("jacoco")
}

android {
    namespace = "com.example.habitflow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.habitflow"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableAndroidTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")
    // Firebase BOM (Bill of Materials) - manages Firebase versions
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))  // Update to latest
    // Firebase Analytics (Required for Firebase to work) and Authentication (For login, optional but useful)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    // Firebase Firestore (For user habits storage)
    implementation("com.google.firebase:firebase-firestore")
    // Firebase Realtime Database (Optional if using Firestore)
    implementation("com.google.firebase:firebase-database")
    // Firebase Cloud Storage (For storing habit-related images, optional)
    implementation("com.google.firebase:firebase-storage")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-text:1.5.0")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.ui:ui-text:1.5.0")
    implementation("androidx.compose.foundation:foundation:1.5.0") // Required for text input
    implementation("androidx.compose.runtime:runtime:1.5.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.core.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Unit test tools
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("connectedDebugAndroidTest")
    val fileFilter = listOf(
        "**/R.class", "**/R$*.class",
        "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*"
    )
    val mainSrc = "${project.projectDir}/src/main/java"
    val kotlinSrc = "${project.projectDir}/src/main/kotlin"
    val javaClassTree = fileTree("${layout. buildDirectory}/intermediates/javac/debug") {
        exclude(fileFilter)
    }
    val kotlinClassTree = fileTree("${layout. buildDirectory}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    classDirectories.setFrom(files(javaClassTree, kotlinClassTree))
    sourceDirectories.setFrom(files(mainSrc, kotlinSrc))
    executionData.setFrom(fileTree("${layout. buildDirectory}/outputs/code_coverage/debugAndroidTest/connected") {
        include("**/coverage.ec")
    })
    reports {
        html.required.set(true)
        html.outputLocation.set(file("${layout. buildDirectory}/reports/jacoco/html"))
        xml.required.set(true)
    }
}

apply(plugin = "com.google.gms.google-services")