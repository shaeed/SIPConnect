import java.io.ByteArrayOutputStream
import org.gradle.process.ExecOperations

abstract class GitHelper @Inject constructor(
    private val execOps: ExecOperations
) {
    fun commitCount(): Int {
        val stdout = ByteArrayOutputStream()
        execOps.exec {
            commandLine("git", "rev-list", "--count", "HEAD")
            standardOutput = stdout
        }
        return stdout.toString().trim().toInt()
    }
}

val gitHelper = objects.newInstance(GitHelper::class.java)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.shaeed.fcmclient"
    compileSdk = 36

    defaultConfig {
        val commitCount = gitHelper.commitCount()

        applicationId = "com.shaeed.fcmclient"
        minSdk = 26
        targetSdk = 36
        versionCode = commitCount
        versionName = "1.0.$commitCount"
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.appcompat)
    // implementation(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.room.runtime)
    implementation(libs.annotations)
    implementation(libs.androidx.navigation.compose.v277)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.libphonenumber)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)
    implementation (libs.androidx.material.icons.extended)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.intellij" && requested.name == "annotations") {
            useTarget("org.jetbrains:annotations:26.0.2") // resolve conflict
            because("Avoid duplicate annotations between JetBrains and IntelliJ")
        }
    }
}

//fun getGitCommitCount(): Int {
//    val stdout = ByteArrayOutputStream()
//    project.exec {
//        commandLine("git", "rev-list", "--count", "HEAD")
//        standardOutput = stdout
//    }
//
//    return stdout.toString().trim().toInt()
//}

