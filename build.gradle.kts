//buildscript {
//    configurations.all {
//        resolutionStrategy {
//            force("org.apache.commons:commons-lang3:3.18.0")
//        }
//    }
//}
//
//allprojects {
//    configurations.all {
//        resolutionStrategy {
//            force("org.apache.commons:commons-lang3:3.18.0")
//        }
//    }
//}

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}
