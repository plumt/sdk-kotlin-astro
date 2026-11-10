pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "sdk-kotlin-astro"

include(":lunar")
// 발행하지 않는 확인용 Android 앱 — R8 release 빌드와 실기기 호출 확인
include(":sample")
