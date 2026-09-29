plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProps = java.util.Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun getSecureProp(key: String, defaultVal: String = ""): String {
    return System.getenv(key)
        ?: localProps.getProperty(key)?.takeIf { it.isNotBlank() }
        ?: defaultVal
}

android {
    namespace = "com.mcbdone.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mcbdone.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val supabaseUrl = getSecureProp("SUPABASE_URL", "https://cvppveogubeudebsmazd.supabase.co")
        val supabaseAnon = getSecureProp("SUPABASE_ANON_KEY", "")
        val googleWebId = getSecureProp("GOOGLE_WEB_CLIENT_ID", "")
        val googleWebSecret = getSecureProp("GOOGLE_WEB_CLIENT_SECRET", "")
        val googleAndroidId = getSecureProp("GOOGLE_ANDROID_CLIENT_ID", "")

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnon\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebId\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_SECRET", "\"$googleWebSecret\"")
        buildConfigField("String", "GOOGLE_ANDROID_CLIENT_ID", "\"$googleAndroidId\"")
    }

    signingConfigs {
        create("mcbdConfig") {
            storeFile = file("${rootDir}/mcbd-debug.keystore")
            storePassword = "mcbd123"
            keyAlias = "mcbdkey"
            keyPassword = "mcbd123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("mcbdConfig")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("mcbdConfig")
        }
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Google Auth & Credentials
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Network & Serialization
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Image loading
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
