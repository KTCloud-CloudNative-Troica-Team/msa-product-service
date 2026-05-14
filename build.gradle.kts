import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    java
    kotlin("jvm") version "2.1.0" apply false
    kotlin("plugin.spring") version "2.1.0" apply false
    kotlin("plugin.jpa") version "2.1.0" apply false
    id("org.springframework.boot") version "3.5.14" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false

    // R-45 (평가 심화 (2)-1): SonarCloud 정적 분석 + 커버리지 게이트.
    // sonar task는 모든 subproject의 test + jacocoTestReport 결과를 자동 집계함.
    id("org.sonarqube") version "5.1.0.4882"
}

// R-45: SonarCloud public 무료 plan 사용 (msa-product-service는 public org repo).
// CI에서 SONAR_TOKEN secret 주입 필요 (org-level secret 권장).
// Quality Gate 통과까지 sonar task가 대기 → 미통과 시 fail → CI fail 전파됨.
sonar {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "ktcloud-cloudnative-troica-team")
        property("sonar.projectKey", "KTCloud-CloudNative-Troica-Team_msa-product-service")
        property("sonar.qualitygate.wait", "true")
        // JaCoCo XML report 경로 — 멀티모듈 fan-in. 절대 경로보다 상대 경로 권장.
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "**/build/reports/jacoco/test/jacocoTestReport.xml",
        )
        // 생성 코드(Protobuf 등) 분석 제외 → 노이즈 감소.
        property("sonar.exclusions", "**/generated/**, **/build/**")
    }
}

allprojects {
    group = "com.troica.msa"
    version = providers.gradleProperty("version").get()

    repositories {
        mavenCentral()
        // 로컬 dev: ./gradlew publishToMavenLocal 한 common-libs 사용
        mavenLocal()
        // 정식 경로: GitHub Packages (common-libs v0.2.0 stable)
        maven {
            name = "GitHubPackagesCommonLibs"
            url = uri("https://maven.pkg.github.com/KTCloud-CloudNative-Troica-Team/msa-common-libs")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                    ?: providers.gradleProperty("gpr.user").orNull
                password = System.getenv("GITHUB_TOKEN")
                    ?: providers.gradleProperty("gpr.token").orNull
            }
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")
    // R-45: JaCoCo로 커버리지 측정 → SonarCloud가 XML report 읽어 게이트 평가함.
    apply(plugin = "jacoco")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
        jvmToolchain(21)
    }

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.14")
        }
    }

    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
    }

    // test 실행 후 JaCoCo XML report 자동 생성 — Sonar가 이걸로 커버리지 계산함.
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        finalizedBy(tasks.matching { it.name == "jacocoTestReport" })
    }
    tasks.withType<org.gradle.testing.jacoco.tasks.JacocoReport>().configureEach {
        dependsOn(tasks.withType<Test>())
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}
