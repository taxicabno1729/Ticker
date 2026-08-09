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
    lint {
        // Advisory checks intentionally off:
        // - LogNotTimber: the app deliberately uses android.util.Log (Timber is
        //   only on the classpath transitively via Reown).
        // - GradleDependency / NewerVersionAvailable / AndroidGradlePluginVersion:
        //   dependency upgrades are done as dedicated, tested changes, not
        //   chased per lint run.
        // - GlobalOptionInConsumerRules: flags proguard rules shipped inside the
        //   Reown artifacts; not fixable from this project.
        disable += listOf(
            "LogNotTimber",
            "GradleDependency",
            "NewerVersionAvailable",
            "AndroidGradlePluginVersion",
            "GlobalOptionInConsumerRules"
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.web3j.core)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.okhttp)
    implementation(libs.slf4j.nop)
    implementation(platform(libs.reown.android.bom))
    implementation(libs.reown.android.core)
    implementation(libs.reown.appkit)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.security.crypto)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
