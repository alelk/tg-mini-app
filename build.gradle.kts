import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "io.github.alelk"

val appVersionFile = projectDir.resolve("app.version")
val versionName: String = project.findProperty("version") as? String
    ?: if (appVersionFile.exists()) {
        appVersionFile.readLines().firstOrNull { it.isNotBlank() }?.trim() ?: "2.0.0"
    } else {
        "2.0.0"
    }
version = versionName

val composeVersion = property("compose.version") as String

repositories {
    google()
    mavenCentral()
}

val detectedChromeBin = System.getenv("CHROME_BIN")
    ?: file("/home/agent/.local/bin/google-chrome").takeIf { it.exists() }?.absolutePath
    ?: file("/usr/local/bin/chromium").takeIf { it.exists() }?.absolutePath
    ?: file("/usr/bin/chromium").takeIf { it.exists() }?.absolutePath
    ?: file("/usr/bin/google-chrome").takeIf { it.exists() }?.absolutePath

kotlin {
    compilerOptions {
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
    }
    js {
        outputModuleName.set("mini-app")
        browser {
            testTask {
                detectedChromeBin?.let { environment("CHROME_BIN", it) }
                useKarma { useChromeHeadless() }
            }
        }
        // Compose requires an executable binary so Skiko is bundled for UI tests (CMP-4906).
        binaries.executable()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("mini-app-wasm")
        browser {
            testTask {
                detectedChromeBin?.let { environment("CHROME_BIN", it) }
                useKarma { useChromeHeadless() }
            }
        }
        binaries.executable()
    }
    sourceSets {
        webMain.dependencies {
            api("org.jetbrains.compose.runtime:runtime:$composeVersion")
            api("org.jetbrains.compose.foundation:foundation:$composeVersion")
            api("org.jetbrains.compose.ui:ui:$composeVersion")
        }
        webTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
            implementation("org.jetbrains.compose.ui:ui-test:$composeVersion")
        }
    }
}

val hasSigningKey = providers.gradleProperty("signingInMemoryKey").isPresent

mavenPublishing {
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))
    if (hasSigningKey) {
        signAllPublications()
    }

    coordinates(group.toString(), "tg-mini-app", version.toString())

    pom {
        name.set("Telegram mini app KMP")
        description.set("Library for creating telegram mini apps with Kotlin and Compose Multiplatform.")
        url.set("https://github.com/alelk/tg-mini-app")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("kirillNay")
                name.set("Kirill Nayduik")
                email.set("kirill.nayduikkn1@gmail.com")
            }
            developer {
                id.set("alelk")
                name.set("Alexey Elkin")
            }
        }
        scm {
            url.set("https://github.com/alelk/tg-mini-app")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/alelk/tg-mini-app")
            credentials {
                username = providers.environmentVariable("GITHUB_USER").getOrElse("alelk")
                password = providers.environmentVariable("GITHUB_TOKEN").orNull
            }
        }
    }
}
