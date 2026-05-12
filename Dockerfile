# syntax=docker/dockerfile:1.7

# ===== Stage 1: build =====
# Alpine(musl libc) 대신 glibc 기반 이미지 사용.
# 이유: protoc-gen-grpc-java가 Maven Central에서 받는 바이너리는 glibc 링크라
# Alpine에서는 "program not found or is not executable" 에러로 generateProto 실패.
# build stage 결과물(layered JAR)은 stage 2에서 추출되어 alpine runtime으로 옮겨지므로
# 최종 image 크기/보안 표면은 동일.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Gradle wrapper + 의존성 메타만 먼저 복사 (캐시 최적화).
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
COPY product/build.gradle.kts product/
COPY product-service/build.gradle.kts product-service/
RUN chmod +x gradlew

# GitHub Packages 인증 (common-libs 의존성 해결).
ARG GPR_USER
ARG GPR_TOKEN
RUN if [ -n "$GPR_USER" ] && [ -n "$GPR_TOKEN" ]; then \
      mkdir -p /root/.gradle && \
      echo "gpr.user=$GPR_USER" > /root/.gradle/gradle.properties && \
      echo "gpr.token=$GPR_TOKEN" >> /root/.gradle/gradle.properties ; \
    fi
RUN ./gradlew dependencies --no-daemon || true

# 소스 복사 후 빌드.
COPY product/src product/src
COPY product-service/src product-service/src
RUN ./gradlew :product-service:bootJar --no-daemon -x test

# ===== Stage 2: extract layers =====
FROM eclipse-temurin:21-jre-alpine AS layers
WORKDIR /app
COPY --from=build /app/product-service/build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ===== Stage 3: runtime =====
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app

COPY --from=layers /app/dependencies/ ./
COPY --from=layers /app/spring-boot-loader/ ./
COPY --from=layers /app/snapshot-dependencies/ ./
COPY --from=layers /app/application/ ./

EXPOSE 8001 9001
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
