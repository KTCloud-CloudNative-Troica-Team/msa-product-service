# syntax=docker/dockerfile:1.7

# R-27 (a): 호스트의 `./gradlew build`가 이미 테스트 + bootJar 완료 → Docker는
# packaging만 담당 (~1.5분 절약). 기존 3-stage 빌드 (build → layers → runtime)
# 에서 build stage 제거.
#
# 사전 조건:
# - CI workflow (또는 로컬)에서 `./gradlew :product-service:bootJar` 또는
#   `./gradlew build` 가 먼저 실행되어 `product-service/build/libs/*.jar`가
#   존재해야 함. ci.yml의 build-test step이 이를 보장.
#
# 부가 효과:
# - build stage가 없어서 glibc 베이스 (eclipse-temurin:21-jdk) 불필요 →
#   alpine만 사용. protoc-gen-grpc-java도 호스트에서 실행되므로 musl 호환성
#   이슈 (TROUBLESHOOTING §3.1) 자동 회피.
# - GitHub Packages 인증 ARG (GPR_USER/GPR_TOKEN)도 제거 → 호스트가 처리.

# ===== Stage 1: layered jar extraction =====
FROM eclipse-temurin:21-jre-alpine AS layers
WORKDIR /app
COPY product-service/build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ===== Stage 2: runtime =====
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
