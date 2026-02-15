plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.liveticker"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.liveticker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "INFURA_PROJECT_ID", "\"${project.findProperty("INFURA_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "REOWN_PROJECT_ID", "\"${project.findProperty("REOWN_PROJECT_ID") ?: ""}\"")

        // Multi-chain RPC URLs
        buildConfigField("String", "RPC_ETHEREUM", "\"https://mainnet.infura.io/v3/${project.findProperty("INFURA_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "RPC_POLYGON", "\"https://polygon-mainnet.infura.io/v3/${project.findProperty("INFURA_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "RPC_ARBITRUM", "\"https://arbitrum-mainnet.infura.io/v3/${project.findProperty("INFURA_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "RPC_OPTIMISM", "\"https://optimism-mainnet.infura.io/v3/${project.findProperty("INFURA_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "RPC_BASE", "\"https://base-mainnet.infura.io/v3/${project.findProperty("INFURA_PROJECT_ID") ?: ""}\"")
    }

    signingConfigs {
        create("release") {
            // Configure these in local.properties or CI environment
            storeFile = file(project.findProperty("RELEASE_STORE_FILE") ?: "release-keystore.jks")
            storePassword = (project.findProperty("RELEASE_STORE_PASSWORD") ?: "") as String
            keyAlias = (project.findProperty("RELEASE_KEY_ALIAS") ?: "") as String
            keyPassword = (project.findProperty("RELEASE_KEY_PASSWORD") ?: "") as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.web3j:core:4.8.7-android")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.slf4j:slf4j-nop:1.7.32")
    implementation(platform("com.reown:android-bom:1.6.2"))
    implementation("com.reown:android-core")
    implementation("com.reown:appkit")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
