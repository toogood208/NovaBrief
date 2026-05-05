import org.gradle.kotlin.dsl.application
import org.gradle.api.tasks.Copy
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

// ─── PLUGINS ────────────────────────────────────────────────────────────────
// These activate the Kotlin Multiplatform + Compose tooling for this module.
plugins {
    alias(libs.plugins.kotlin.multiplatform)                  // enables KMP (multi-target builds)
    alias(libs.plugins.android.kotlin.multiplatform.library)  // Android library support inside KMP
    alias(libs.plugins.android.lint)                          // Android lint checks
    alias(libs.plugins.kotlin.compose)                        // enables Compose compiler for Kotlin
    alias(libs.plugins.compose.multiplatform)                 // enables Compose Multiplatform (cross-platform UI)
    alias(libs.plugins.sqldelight)
}

kotlin {

    // ─── TARGET: ANDROID ────────────────────────────────────────────────────
    // Declares that this module has an Android build target.
    // Code in androidMain/ gets compiled for Android.
    android {
        namespace = "com.example.shared"
        compileSdk {
            version = release(36) {
                minorApiLevel = 1
            }
        }
        minSdk = 24

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    // ─── TARGET: DESKTOP (JVM) ───────────────────────────────────────────────
    // Declares a JVM target named "desktop".
    // This is what makes Android Studio show "shared [desktop]".
    // Code in desktopMain/ + jvmMain/ gets compiled for this target.
    // jvmMain contains shared HTTP code (Retrofit) used by both Android and Desktop.
    jvm("desktop")

    // ─── TARGET: WEB (JS IR) ─────────────────────────────────────────────────
    // enabled browser run tasks are available again.
    // ─── TARGET: WEB (JS IR) ─────────────────────────────────────────────────
    js(IR) {
        browser {
            // Add this line to disable source maps and speed up the build
            commonWebpackConfig {
                sourceMaps = false
            }
        }
        binaries.executable()
    }

    // ─── TARGET: iOS ────────────────────────────────────────────────────────
    // Declares 3 iOS targets (device + simulators).
    // Each produces a .framework binary that Xcode links into the iOS app.
    val xcfName = "sharedKit"

    iosX64 {             // iOS simulator on Intel Mac
        binaries.framework { baseName = xcfName }
    }

    iosArm64 {           // real iOS device (ARM64)
        binaries.framework { baseName = xcfName }
    }

    iosSimulatorArm64 {  // iOS simulator on Apple Silicon Mac
        binaries.framework { baseName = xcfName }
    }

    // ─── SOURCE SETS ────────────────────────────────────────────────────────
    // Source sets bind folders to targets.
    // commonMain  → shared by ALL targets
    // jvmMain     → custom intermediate source set for JVM (Android + Desktop)
    // androidMain → Android-only code
    // desktopMain → Desktop-only code
    // iosMain     → iOS-only code
    sourceSets {

        // Shared across every platform (Android, Desktop, iOS, Web, etc.)
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)

                // Compose Multiplatform UI — shared across all targets
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(libs.sqldelight.runtime)
            }
        }

        // Tests shared across every platform
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        // JVM-shared dependencies (intermediate source set for Android + Desktop)
        // This is custom - not tied to a target, but declared manually
        val jvmMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.retrofit)
                implementation(libs.retrofit.gson)
                implementation(libs.okhttp.logging)
                implementation(libs.gson)
                implementation(libs.sqldelight.coroutines)
            }
        }

        // Android-only dependencies go here
        androidMain {
            dependsOn(jvmMain)
            dependencies {
                implementation(libs.coil.compose)
                implementation(libs.sqldelight.android.driver)
            }
        }

        // Desktop-only dependencies go here
        // compose.desktop.currentOs pulls in the right Skia renderer for Windows/macOS/Linux
        val desktopMain by getting {
            dependsOn(jvmMain)
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.sqldelight.sqlite.driver)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }


        // Android instrumented test dependencies
        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.core)
                implementation(libs.androidx.junit)
                implementation(libs.androidx.runner)
            }
        }

        // iOS-only dependencies go here
        iosMain {
            dependencies {
                // iOS-specific deps (optional)
            }
        }
    }
}

sqldelight {
    databases {
        create("NovaBriefDatabase") {
            packageName.set("com.example.shared.feature.news.data.local.db")
        }
    }
}

// ─── DESKTOP APP RUNNER CONFIG ───────────────────────────────────────────────
// This does NOT declare the desktop target (jvm("desktop") above does that).
// This tells Gradle HOW to run/package the desktop app:
//   - mainClass: which Kotlin class to launch (MainKt = top-level main() in Main.kt)
//   - enables tasks: desktopRun, hotRunDesktop, createDistributable, packageMsi, etc.
compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "NovaBrief"
            packageVersion = "1.0.0"

            windows {
                shortcut = true
                menu = true

            }
        }
    }
}

val webNewsApiKey: String = System.getenv("NEWS_API_KEY") ?: run {
    val localPropertiesFile = rootProject.file("local.properties")
    if (!localPropertiesFile.isFile) {
        ""
    } else {
        localPropertiesFile.inputStream().use { stream ->
            Properties().apply { load(stream) }
                .getProperty("NEWS_API_KEY")
                .orEmpty()
                .trim()
        }
    }
}

fun escapeKotlinString(value: String): String = buildString(value.length) {
    value.forEach { ch ->
        when (ch) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(ch)
        }
    }
}

val generateWebApiKeySource by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/jsMain/kotlin")
    val outputFile = outputDir.map { it.file("WebApiKey.generated.kt") }

    inputs.property("newsApiKey", webNewsApiKey)
    outputs.file(outputFile)

    doLast {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |internal const val WEB_NEWS_API_KEY: String = "${escapeKotlinString(webNewsApiKey)}"
            |""".trimMargin()
        )
    }
}

kotlin.sourceSets.named("jsMain") {
    kotlin.srcDir(layout.buildDirectory.dir("generated/jsMain/kotlin"))
}

tasks.named("compileKotlinJs") {
    dependsOn(generateWebApiKeySource)
}

// Ensure webpack dev server root has index.html so opening '/' renders the app, not a file listing.
val copyJsIndexHtml by tasks.registering(Copy::class) {
    from(layout.projectDirectory.file("src/jsMain/resources/index.html"))
    into(rootProject.layout.buildDirectory.dir("js/packages/${rootProject.name}-${project.name}"))
    // Re-copy when key changes so dev webpack always sees the latest injected value.
    inputs.property("newsApiKey", webNewsApiKey)
    expand(mapOf("newsApiKey" to webNewsApiKey))
    filteringCharset = "UTF-8"
}

tasks.matching {
    it.name in setOf(
        "jsBrowserDevelopmentRun",
        "jsBrowserDevelopmentWebpack",
        "jsBrowserProductionWebpack"
    )
}.configureEach {
    dependsOn(copyJsIndexHtml)
}

// Gradle 9+ validation: rootPackageJson reads this directory, so declare task ordering explicitly.
rootProject.tasks.matching {
    it.name == "rootPackageJson"
}.configureEach {
    dependsOn(copyJsIndexHtml)
}

