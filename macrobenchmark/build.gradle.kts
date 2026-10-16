plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "com.example.benchmark"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 28
        targetSdk = 37

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    buildTypes {
        // This benchmark buildType is used for benchmarking, and should function like your
        // release build (for example, with minification on). It"s signed with a debug key
        // for easy local/CI testing.
        create("benchmark1") {
            isDebuggable = true
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.androidTest.useAbsoluteFqcn"] = true
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.uiautomator)
    implementation("androidx.arch.core:core-runtime:2.2.0")
    implementation("androidx.arch.core:core-common:2.2.0")
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark1"
    }
}