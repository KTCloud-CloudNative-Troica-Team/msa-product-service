package dev.ktcloud.black.product.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * R-57 (평가 기본 (3)-1): ProductDomainEntity 단위 테스트.
 *
 * 도메인 entity 는 단순 data class — UUID 자동 생성 + Kotlin data class 의 equality /
 * copy 보존 동작 확인.
 */
@DisplayName("ProductDomainEntity - 상품 도메인 entity")
class ProductDomainEntityTest {

    @Test
    @DisplayName("id 미지정 시 UUID 자동 생성")
    fun `id default 가 UUID randomUUID`() {
        val p = ProductDomainEntity(name = "노트북", description = "Test", price = 1_000_000)

        assertThat(p.id).isNotNull()
        // UUID 는 36 자 형식 (8-4-4-4-12 hex 형태)
        assertThat(p.id.toString()).hasSize(36)
    }

    @Test
    @DisplayName("두 인스턴스의 id 가 서로 달라야 함 (UUID 무작위)")
    fun `각 인스턴스의 UUID 는 unique`() {
        val p1 = ProductDomainEntity(name = "A", description = "a", price = 100)
        val p2 = ProductDomainEntity(name = "B", description = "b", price = 200)

        assertThat(p1.id).isNotEqualTo(p2.id)
    }

    @Test
    @DisplayName("id 가 같으면 data class equality 로 동등 (다른 필드도 같을 때)")
    fun `data class equality`() {
        val fixedId = UUID.randomUUID()
        val p1 = ProductDomainEntity(id = fixedId, name = "X", description = "x", price = 500)
        val p2 = ProductDomainEntity(id = fixedId, name = "X", description = "x", price = 500)

        assertThat(p1).isEqualTo(p2)
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode())
    }

    @Test
    @DisplayName("copy() 로 price 만 변경 — 나머지 필드 보존")
    fun `copy 로 부분 갱신`() {
        val original = ProductDomainEntity(name = "맥북", description = "M3", price = 2_000_000)
        val updated = original.copy(price = 1_800_000)

        assertThat(updated.id).isEqualTo(original.id)
        assertThat(updated.name).isEqualTo("맥북")
        assertThat(updated.description).isEqualTo("M3")
        assertThat(updated.price).isEqualTo(1_800_000)
    }
}
