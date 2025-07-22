import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.sevenzipjbinding)

//            val os = org.gradle.internal.os.OperatingSystem.current()
//            when {
//                os.isWindows -> implementation("net.sf.sevenzipjbinding:sevenzipjbinding-all-windows:16.02-2.01")
//                os.isLinux -> implementation("net.sf.sevenzipjbinding:sevenzipjbinding-all-linux:16.02-2.01")
//                else -> implementation("net.sf.sevenzipjbinding:sevenzipjbinding-all-mac:16.02-2.01")
//            }

            // Use a patched version of sevenzipjbinding-all-platforms with support for Apple silicon
            // See https://github.com/mucommander/mucommander/pull/1237
            implementation(libs.sevenzipjbinding.all.platforms)
            // Enables FileKit dialogs without Compose dependencies
            implementation(libs.filekit.dialogs)
            // Enables FileKit dialogs with Composable utilities
            implementation(libs.filekit.dialogs.compose)
        }
    }
}


compose.desktop {
    application {
        mainClass = "top.ntutn.sevenzip.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "top.ntutn.sevenzip"
            packageVersion = "1.0.0"
        }
    }
}
