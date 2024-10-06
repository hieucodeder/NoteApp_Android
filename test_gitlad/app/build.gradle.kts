plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.example.test_gitlad"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.test_gitlad"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //noinspection UseTomlInstead
    implementation("androidx.core:core-ktx:1.13.1")


    //noinspection UseTomlInstead
    implementation("androidx.appcompat:appcompat:1.7.0")


    //noinspection UseTomlInstead
    implementation("com.google.android.material:material:1.12.0")

    //noinspection UseTomlInstead
    implementation("androidx.activity:activity-ktx:1.9.1")

    //noinspection UseTomlInstead
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")


    //noinspection UseTomlInstead
    implementation("androidx.room:room-runtime:2.6.1")
    //noinspection UseTomlInstead
    implementation("androidx.room:room-ktx:2.6.1")

    //noinspection UseTomlInstead
    implementation("com.github.bumptech.glide:glide:4.16.0")
    //noinspection UseTomlInstead
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.0")

    //noinspection UseTomlInstead
    implementation("com.google.android.material:material:1.12.0")

    //noinspection UseTomlInstead
    testImplementation("junit:junit:4.13.2")
    //noinspection UseTomlInstead
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    //noinspection UseTomlInstead
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    //noinspection UseTomlInstead
    implementation("com.google.code.gson:gson:2.10.1")

    //noinspection UseTomlInstead
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0")

    implementation ("androidx.viewpager2:viewpager2:1.0.0")

    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.vectordrawable:vectordrawable:1.2.0")
    implementation ("com.afollestad.material-dialogs:core:3.3.0")
    implementation ("com.afollestad.material-dialogs:color:3.3.0")
}


