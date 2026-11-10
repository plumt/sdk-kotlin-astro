plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

// Android(D8)·서버 양쪽에서 읽히도록 바이트코드는 Java 11 로 낸다. 빌드 JDK 는 무엇이든 된다
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        // 공개 인터페이스의 기본 구현을 Java 기본 메서드로만 낸다(DefaultImpls 없음) — 인터페이스에 함수를 더해도 구현체가 깨지지 않는다
        jvmDefault.set(org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode.NO_COMPATIBILITY)
    }
    // 가시성을 적지 않은 선언은 컴파일 오류 — 공개 API 는 의도한 것만 연다
    explicitApi()
}

dependencies {
    testImplementation(libs.junit)
}

mavenPublishing {
    coordinates(artifactId = "lunar", version = property("VERSION_NAME").toString())
    publishToMavenCentral()
    signAllPublications()
    pom {
        name.set("astro-lunar")
        description.set("달 위상·거리·월출월몰·24절기를 네트워크 없이 계산하는 Kotlin 라이브러리")
        inceptionYear.set("2026")
    }
}
