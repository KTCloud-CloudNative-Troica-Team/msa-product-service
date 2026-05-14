plugins {
    kotlin("plugin.jpa")
}

dependencies {
    // common-libs (모노레포의 :common 모듈을 GitHub Packages 의존성으로 치환)
    implementation("com.troica.msa:common:0.3.1")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    runtimeOnly("com.h2database:h2")

    // R-57: 단위 테스트 — JUnit 5 + AssertJ + Mockito
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Gradle 8.x + JUnit Platform 1.12+ 에서 launcher 명시 필요 (OutputDirectoryProvider)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
