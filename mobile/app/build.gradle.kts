import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Opcional en local.properties: api.url=http://127.0.0.1:8000/api/ (teléfono físico con `adb reverse tcp:8000 tcp:8000`).
val propiedadesLocales = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}
val apiDebug: String = propiedadesLocales.getProperty("api.url", "http://10.0.2.2:8000/api/")

android {
    namespace = "com.yelpme.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.yelpme.app"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_URL", "\"$apiDebug\"")
        }
        release {
            buildConfigField("String", "API_URL", "\"https://yelpme-api.up.railway.app/api/\"")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.swiperefreshlayout)
    implementation(libs.splashscreen)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.glide)
    implementation(libs.play.services.location)
    implementation(libs.osmdroid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
