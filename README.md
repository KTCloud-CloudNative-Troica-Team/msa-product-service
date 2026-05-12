# msa-product-service

Troica Market Service의 **상품 도메인** 마이크로서비스. 단순 CRUD + gRPC API.

> SPEC + ADR: [msa-argocd-manifest/docs](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/tree/main/docs)
> 트러블슈팅: [TROUBLESHOOTING.md](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md)

---

## 빠른 시작 (L2 — 로컬 docker로 끝까지 실행)

### 사전 요구사항

| 항목 | 버전 |
|---|---|
| Java | 21 (Temurin) |
| Docker | 24+ |
| GitHub PAT | `read:packages` (common-libs 다운로드용) |

### 1. GH Packages 인증 (1회)

`~/.gradle/gradle.properties`:
```
gpr.user=<github-username>
gpr.token=<PAT-with-read:packages>
```

### 2. PostgreSQL 컨테이너 띄우기

```bash
docker run -d --name pg-product \
  -p 7001:5432 \
  -e POSTGRES_USER=product-service \
  -e POSTGRES_PASSWORD=product-service \
  -e POSTGRES_DB=product_db \
  postgres:18-alpine

# 확인
docker ps | grep pg-product
```

### 3. 빌드 + 테스트

```bash
./gradlew build
```

기대: `BUILD SUCCESSFUL`, `product-service/build/libs/*.jar` 생성.

### 4. 로컬 실행 (Gradle bootRun)

```bash
./gradlew :product-service:bootRun --args='--spring.profiles.active=dev'
```

기대 로그:
```
Started ProductServiceApplicationKt in X seconds
Tomcat started on port 8001 (http)
gRPC server started on port 9001
```

### 5. 검증

```bash
# Health endpoint
curl -s http://localhost:8001/healthz | jq
# {"status":"UP",...}

# gRPC service 목록
grpcurl -plaintext localhost:9001 list
# dev.ktcloud.black.product.service.ProductService

# Prometheus metrics
curl -s http://localhost:8001/prometheus | head -20
```

### 6. Docker로 실행 (alternative)

```bash
# bootJar 생성
./gradlew :product-service:bootJar

# image build
docker build -t msa/product-service:local .

# 실행 — PostgreSQL을 host.docker.internal로 참조
docker run --rm \
  -p 8001:8001 -p 9001:9001 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e PRODUCT_DB_HOST=host.docker.internal \
  msa/product-service:local
```

### 7. 정리

```bash
docker rm -f pg-product
docker rmi msa/product-service:local      # local image 제거 (선택)
```

---

## 모듈 구조

```
msa-product-service/
├── product/             # 도메인 라이브러리 — JPA entities, services, ports (hexagonal)
└── product-service/     # Spring Boot 앱 — gRPC controller + main + profile 설정
```

### 책임 분리

- **`product`**: 비즈니스 로직 + 도메인 계약. 다른 모듈 의존 없음 (common-libs만).
- **`product-service`**: 진입점 (`@SpringBootApplication`), gRPC controller adapter, profile별 설정.

---

## 포트

| 프로토콜 | 포트 | 용도 |
|---------|------|------|
| HTTP | 8001 | Actuator (`/healthz`, `/prometheus`, `/info`) |
| gRPC | 9001 | `ProductService` (other services + api-gateway) |

---

## 환경 변수

| 변수 | 기본값 | 설명 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | (none) | `dev` / `prod` |
| `PRODUCT_DB_HOST` | localhost | PostgreSQL host |
| `PRODUCT_DB_PORT` | 7001 | PostgreSQL port |
| `PRODUCT_DB_NAME` | product_db | DB name |
| `PRODUCT_DB_USERNAME` | product-service | DB user |
| `PRODUCT_DB_PASSWORD` | product-service | DB password |
| `JPA_DDL_AUTO` | validate (prod) / update (dev) | Hibernate 스키마 정책 |
| `JPA_SHOW_SQL` | false | SQL 로그 |
| `SERVER_PORT` | 8001 | HTTP listen |
| `GRPC_SERVER_PORT` | 9001 | gRPC listen |

---

## 외부 의존성

| 의존 | 용도 | 로컬 실행 시 |
|------|------|-------------|
| PostgreSQL `product_db` | 도메인 데이터 | `postgres:18-alpine` 컨테이너 (위 STEP 2) |
| `com.troica.msa:common:0.3.1` | JPA/QueryDSL config (`QuerydslConfig`), Base 엔티티, 공통 예외 | GH Packages 자동 |

**Kafka, Redis 미사용.**

---

## CI/CD

`.github/workflows/ci.yml`:
- **PR**: `build-test` (호스트에서 `./gradlew build` — 테스트 포함)
- **Push to main**: build → Docker → Trivy → ECR push → manifest auto-bump PR
  - push-gated step은 `vars.AWS_DEPLOYMENTS_ENABLED == 'true'` 일 때만 실행

빌드 시간: ~2분 25초 (R-27 (a) 적용 후, [TROUBLESHOOTING §4.3](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md#43-ci-빌드-4분--25분-r-27-a-적용-후)).

---

## Docker 이미지 구조

R-27 (a) 적용 후 **2-stage** Dockerfile:
1. **Stage 1 (layers)**: 호스트가 만든 `product-service/build/libs/*.jar`를 layered extract
2. **Stage 2 (runtime)**: `eclipse-temurin:21-jre-alpine` + non-root user

호스트에서 Gradle 빌드가 끝나야 Docker build 가능 (Docker는 packaging만 담당).

---

## 트러블슈팅

- **공용 라이브러리 다운로드 실패** → `~/.gradle/gradle.properties`의 `gpr.user`/`gpr.token` 확인 + PAT의 `read:packages` 권한
- **`@Configuration class may not be final`** → common-libs 0.3.1 이상 사용 (R-38 fix). 0.2.0/0.3.0는 `QuerydslConfig` 가 final이라 startup 실패. [TROUBLESHOOTING §1.7](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md#17-kotlin-configuration-class가-final--spring-cglib-proxy-실패-r-38)
- **PostgreSQL connection refused** → STEP 2의 컨테이너 띄움 확인. `docker ps | grep pg-product`
- **Docker build에서 `protoc-gen-grpc-java program not found`** → 이전 alpine build stage 이슈. R-27 (a)에서 build stage 제거됨 (Docker가 더 이상 Gradle 실행 안 함). [TROUBLESHOOTING §3.1](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md#31-alpinemusl--protoc-gen-grpc-java-glibc-비호환)

---

## 관련 문서

- [msa-argocd-manifest](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest) — GitOps 매니페스트 (`applications/values/product-service/`)
- [msa-common-libs](https://github.com/KTCloud-CloudNative-Troica-Team/msa-common-libs) — `com.troica.msa:common` 의존성
- [TROUBLESHOOTING.md](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/docs/TROUBLESHOOTING.md) — 디버깅 자료
