plugins {
    alias(libs.plugins.gptmap.android.library.compose)
    alias(libs.plugins.gptmap.android.feature)
}

android {
    namespace = "com.espressodev.gptmap.feature.login"
}

dependencies {
    implementation(projects.core.google)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.auth)
}