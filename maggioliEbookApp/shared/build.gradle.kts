import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("plugin.serialization") version "1.7.10"
    id("com.squareup.sqldelight")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    id("com.rickclephas.kmp.nativecoroutines") version "0.13.0"
}

version = "1.0"

kotlin {
    android()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosMaggioliEbookApp/Podfile")
        framework {
            baseName = "shared"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                // https://mvnrepository.com/artifact/io.ktor/ktor-client-core
                implementation("io.ktor:ktor-client-core:2.1.1")
                // https://mvnrepository.com/artifact/io.ktor/ktor-client-serialization
                implementation("io.ktor:ktor-client-serialization:2.1.1")
                // https://mvnrepository.com/artifact/io.ktor/ktor-client-json
                implementation("io.ktor:ktor-client-json:2.1.1")
                // https://mvnrepository.com/artifact/io.ktor/ktor-client-logging
                implementation("io.ktor:ktor-client-logging:2.1.1")
                // https://mvnrepository.com/artifact/io.ktor/ktor-client-content-negotiation
                implementation("io.ktor:ktor-client-content-negotiation:2.1.1")
                // https://mvnrepository.com/artifact/io.ktor/ktor-serialization-kotlinx-json
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.1")

                // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-serialization-core
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.0")
                // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-serialization-json
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
                // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-datetime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

                // https://mvnrepository.com/artifact/com.liftric/kvault
                implementation("com.liftric:kvault:1.9.0")

                // https://mvnrepository.com/artifact/com.squareup.sqldelight/runtime
                implementation("com.squareup.sqldelight:runtime:1.5.3")
                // https://mvnrepository.com/artifact/com.squareup.sqldelight/coroutines-extensions
                implementation("com.squareup.sqldelight:coroutines-extensions:1.5.3")

                // https://mvnrepository.com/artifact/io.insert-koin/koin-core
                implementation("io.insert-koin:koin-core:3.2.0")

                // https://mvnrepository.com/artifact/io.github.aakira/napier
                implementation("io.github.aakira:napier:2.6.1")

                // https://mvnrepository.com/artifact/junit/junit
                implementation("junit:junit:4.13.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))

                // https://mvnrepository.com/artifact/junit/junit
                implementation("junit:junit:4.13.2")

                // https://mvnrepository.com/artifact/io.insert-koin/koin-test
                implementation("io.insert-koin:koin-test:3.2.0")
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                // https://mvnrepository.com/artifact/io.ktor/ktor-client-android
                implementation("io.ktor:ktor-client-android:2.1.1")
                implementation("io.insert-koin:koin-android:3.2.0")
                // https://mvnrepository.com/artifact/com.squareup.sqldelight/android-driver
                implementation("com.squareup.sqldelight:android-driver:1.5.3")
            }
        }
        val androidTest by getting {
            dependsOn(commonTest)
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                // https://mvnrepository.com/artifact/io.ktor/ktor-client-ios
                implementation("io.ktor:ktor-client-ios:2.1.1")
                // https://mvnrepository.com/artifact/com.squareup.sqldelight/native-driver
                implementation("com.squareup.sqldelight:native-driver:1.5.3")
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    compileSdk = 32
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 28
        targetSdk = 32
    }
}

tasks.build.dependsOn("generateSqlDelightInterface")
dependencies {
    implementation("androidx.test.ext:junit-ktx:1.1.3")
}

detekt {
    // The directories where detekt looks for source files.
    // Defaults to `files("src/main/java", "src/test/java", "src/main/kotlin", "src/test/kotlin")`.
    source = files(
        "src/androidMain",
        "src/androidTest",
        "src/commonMain",
        "src/commonTest",
        "src/iosMain",
        "src/iosTest"
    )
    ignoreFailures = true
}

sqldelight {
    database("MaggioliEbookDB") {
        sourceFolders = listOf("sqldelight")
        packageName = "it.filo.maggioliebook.db"
    }
}
