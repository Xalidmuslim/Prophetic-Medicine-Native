plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
}
android {
  namespace = "com.xalid.meditsinaproroka.nativeapp"
  compileSdk = 35
  defaultConfig {
    // The .preview suffix protects the existing installed v1.0.0 and its user data.
    applicationId = "com.xalid.meditsinaproroka.nativeapp.preview"
    minSdk = 26
    targetSdk = 35
    versionCode = 1
    versionName = "0.1.0-native-preview"
  }
  buildFeatures { compose = true }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }
  packaging {
    resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
  }
}
dependencies {
  implementation(platform("androidx.compose:compose-bom:2024.12.01"))
  implementation("androidx.activity:activity-compose:1.10.0")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.animation:animation")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
  testImplementation("junit:junit:4.13.2")
  // JVM test runner needs a real JSONObject implementation, not the Android mockable stub.
  testImplementation("org.json:json:20240303")
  debugImplementation("androidx.compose.ui:ui-tooling")
}
