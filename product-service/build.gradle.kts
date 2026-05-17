import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("com.google.protobuf") version "0.9.5"
}

object Versions {
    const val GRPC = "1.75.0"            // io.grpc:grpc-* — 1.75.0 fixes CVE-2025-55163 (Netty MadeYouReset HTTP/2 DDoS)
    const val GRPC_KOTLIN = "1.4.1"      // grpc-kotlin-stub
    const val PROTOBUF = "4.34.1"        // protobuf-java/kotlin
}

dependencies {
    implementation(project(":product"))

    implementation("com.troica.msa:common:0.3.1")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // R-65 (평가 기본 (3)-3 필수): Prometheus text format 의 /actuator/prometheus
    // endpoint 노출. spring-boot-starter-actuator 만으로는 /actuator/metrics (JSON)
    // 만 노출되고 /actuator/prometheus 는 404. 본 의존성 + msa-argocd-manifest
    // PR #120 의 MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE env 와 함께 노출 →
    // ServiceMonitor scrape OK → Grafana Troica dashboard panel data 표시.
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // gRPC server starter + grpc-kotlin + protobuf
    implementation("net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE")
    implementation("com.google.protobuf:protobuf-java:${Versions.PROTOBUF}")
    implementation("com.google.protobuf:protobuf-kotlin:${Versions.PROTOBUF}")
    implementation("io.grpc:grpc-protobuf:${Versions.GRPC}")
    implementation("io.grpc:grpc-stub:${Versions.GRPC}")
    implementation("io.grpc:grpc-kotlin-stub:${Versions.GRPC_KOTLIN}")
    implementation("io.grpc:grpc-netty-shaded:${Versions.GRPC}")

    // postgresql 42.7.11 fixes CVE-2026-42198 (Client DoS via malicious SCRAM-SHA-256 auth).
    // Spring Boot 3.5.14 BOM은 42.7.10 그대로 → 명시적 version override.
    // 다음 SB patch에 42.7.11이 포함되면 version 빼고 BOM 관리로 복귀 가능.
    runtimeOnly("org.postgresql:postgresql:42.7.11")
}

sourceSets {
    main {
        java.srcDirs(
            "build/generated/source/proto/main/java",
            "build/generated/source/proto/main/kotlin",
        )
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Versions.PROTOBUF}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${Versions.GRPC}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${Versions.GRPC_KOTLIN}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("kotlin")
            }
            task.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

springBoot {
    mainClass.set("dev.ktcloud.black.product.service.ProductServiceApplicationKt")
}
