import android.annotation.SuppressLint
import com.android.build.api.variant.FilterConfiguration
import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

val dateFormat = SimpleDateFormat("yyMMddHH")

android {
    namespace = "io.github.lumyuan.turingbox"
    compileSdk = 33

    signingConfigs {
        create("turing-box-signing") {
            storeFile = file("../turing-key.jks")
            storePassword = "0409.kaly"
            keyAlias = "lumyuan"
            keyPassword = "0409.kaly"
            this.enableV1Signing = true
            this.enableV2Signing = true
            this.enableV3Signing = true
            this.enableV4Signing = true
        }
    }

    defaultConfig {
        applicationId = "io.github.lumyuan.turingbox"
        minSdk = 21

        val appVersionCode = dateFormat.format(Date()).trim().toLong()

        @SuppressLint("ExpiredTargetSdkVersion")
        targetSdk = 28
        versionCode = appVersionCode.toInt()
        versionName = "0.0.1-$appVersionCode"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("turing-box-signing")
        }

        create("release-mini") {
            isShrinkResources = true
            isZipAlignEnabled = true
            isMinifyEnabled = true
            isDebuggable = false
            isJniDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("turing-box-signing")
        }
    }

    val abiCodes = mapOf("armeabi-v7a" to 1, "arm64-v8a" to 2, "x86" to 3, "x86_64" to 4)

    applicationVariants.all {
        val buildType = this.buildType.name
        val variant = this
        outputs.all {
            val name =
                this.filters.find { it.filterType == FilterConfiguration.FilterType.ABI.name }?.identifier
            val baseAbiCode = abiCodes[name]
            if (baseAbiCode != null) {
                //写入cpu架构信息
                variant.buildConfigField("String", "CUP_ABI", "\"${name}\"")
            }
            if (this is ApkVariantOutputImpl) {
                //修改apk名称
                outputFileName = "Turing Box-${defaultConfig.versionName}-${buildType}.apk"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets")
            }
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(libs.android.appcompat)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    implementation(libs.immersionbar.ui)
    implementation(libs.immersionbar.ui.ktx)
}