plugins {
    alias(libs.plugins.gptmap.android.library)
    alias(libs.plugins.gptmap.android.library.compose)
    alias(libs.plugins.gptmap.android.feature)
    alias(libs.plugins.secrets)
}

android {
    namespace = "com.espressodev.gptmap.feature.map"

}

dependencies {
    implementation(libs.maps.compose)
}