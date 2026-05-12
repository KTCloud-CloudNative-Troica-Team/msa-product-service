plugins {
    kotlin("plugin.jpa")
}

dependencies {
    // common-libs (모노레포의 :common 모듈을 GitHub Packages 의존성으로 치환)
    implementation("com.troica.msa:common:0.2.0")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    runtimeOnly("com.h2database:h2")
}
