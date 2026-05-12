# msa-product-service

Troica Market Service의 **상품 도메인** 마이크로서비스. 단순 CRUD 위주.

> Single source of truth: [TROICA_SPEC.md](https://github.com/KTCloud-CloudNative-Troica-Team/msa-argocd-manifest/blob/main/TROICA_SPEC.md)

## 모듈 구조

```
msa-product-service/
├── product/             # 도메인 라이브러리 (JPA entities, services, ports)
└── product-service/     # Spring Boot 앱 (gRPC controller, main, profile 설정)
```

## 포트

| 프로토콜 | 포트 |
|---------|------|
| HTTP (Actuator)   | 8001 |
| gRPC              | 9001 |

## 외부 의존성

| 의존 | 용도 |
|------|------|
| PostgreSQL `product_db` | 도메인 데이터 |
| `com.troica.msa:common:0.2.0` | JPA/QueryDSL config, base 엔티티, 공통 예외 |

Kafka, Redis 미사용.

## 빌드

```bash
./gradlew build
docker build \
  --build-arg GPR_USER=$GITHUB_ACTOR \
  --build-arg GPR_TOKEN=$GITHUB_TOKEN \
  -t msa/product-service:local .
```

## CI

`.github/workflows/ci.yml` 동작은 Phase 3 패턴 동일:
- PR: `build-test` (build + test, GitHub Packages에서 common-libs 받음)
- Push to main: build + ECR push + manifest update 흐름은 `vars.AWS_DEPLOYMENTS_ENABLED == 'true'` 일 때만 동작 (SPEC §7 / BACKLOG R-19)
