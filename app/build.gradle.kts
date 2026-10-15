plugins {
    id("com.android.application")
}

android {
    namespace = "com.camb.shell"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.camb.shell"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters.add("arm64-v8a")
        }
        externalNativeBuild {
            cmake {
                ndkVersion = "27.0.12077973"
                arguments.add("-DANDROID_STL=c++_shared")
                arguments.add("-DCMAKE_CXX_STANDARD=17")
                arguments.add("-DCMAKE_CXX_STANDARD_REQUIRED=ON")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
}

dependencies {

}
