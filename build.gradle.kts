// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.about.libraries) apply false
    alias(libs.plugins.kotlin.dokka)
}


dependencies {
    dokka(project(":core"))
    dokka(project(":ui"))
    dokka(project(":compose"))
    dokka(project(":compose-ui"))
//    dokka(project(":icc"))
//    dokka(project(":jp2"))
}
