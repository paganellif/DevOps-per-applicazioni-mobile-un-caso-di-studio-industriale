plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    id("io.gitlab.arturbosch.detekt")
}

android {
    compileSdk = 32
    defaultConfig {
        applicationId = "it.filo.maggioliebook.android"
        minSdk = 28
        targetSdk = 32
        versionCode = 11
        versionName = "1.3.0" // TODO: update version in @layout/about_fragment
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk.abiFilters.add("armeabi-v7a")
        ndk.abiFilters.add("arm64-v8a")
        ndk.abiFilters.add("x86")
        ndk.abiFilters.add("x86_64")
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv()
                .getOrDefault("RELEASE_KEY_JKS_PATH","my-release-key.jks")
            )
            storePassword = System.getenv()
                .getOrDefault("RELEASE_KEY_STORE_PWD", "pwd")
            keyAlias = System.getenv().getOrDefault("RELEASE_KEY_ALIAS","my-alias")
            keyPassword = System.getenv().getOrDefault("RELEASE_KEY_PWD", "pwd")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    implementation(project(":shared"))

    // https://jitpack.io/#readium/kotlin-toolkit
    implementation("com.github.readium.kotlin-toolkit:readium-shared:2.2.1")
    implementation("com.github.readium.kotlin-toolkit:readium-streamer:2.2.1")
    implementation("com.github.readium.kotlin-toolkit:readium-navigator:2.2.1")
    implementation("com.github.readium.kotlin-toolkit:readium-opds:2.2.1")
    implementation("com.github.readium.kotlin-toolkit:readium-lcp:2.2.1")

    implementation("com.liftric:kvault:1.9.0")

    implementation("joda-time:joda-time:2.10.14")

    implementation("io.insert-koin:koin-core:3.2.0")
    implementation("io.insert-koin:koin-android:3.2.0")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    implementation("com.google.android.material:material:1.6.1")

    implementation("androidx.compose.animation:animation:1.2.1")
    implementation("androidx.compose.foundation:foundation:1.2.1")
    implementation("androidx.compose.material:material-icons-extended:1.2.1")
    implementation("androidx.compose.material:material:1.2.1")

    implementation("androidx.paging:paging-runtime:3.1.1")
    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.annotation:annotation:1.4.0")
    implementation("androidx.fragment:fragment-ktx:1.5.2")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.activity:activity-compose:1.5.1")
    implementation("androidx.activity:activity-ktx:1.5.1")

    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.5.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test.ext:junit:1.1.3")
    testImplementation("androidx.test:rules:1.4.0")
    testImplementation("androidx.test.espresso:espresso-core:3.4.0")
    testImplementation("androidx.test:runner:1.4.0")
    testImplementation("tools.fastlane:screengrab:2.1.1")
    // https://mvnrepository.com/artifact/io.insert-koin/koin-test
    testImplementation("io.insert-koin:koin-test:3.2.0")
    testImplementation("io.insert-koin:koin-test-junit4:3.2.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("tools.fastlane:screengrab:2.1.1")
    // https://mvnrepository.com/artifact/io.insert-koin/koin-test
    androidTestImplementation("io.insert-koin:koin-test:3.2.0")
    androidTestImplementation("io.insert-koin:koin-test-junit4:3.2.0")
}

detekt {
    ignoreFailures = true
}