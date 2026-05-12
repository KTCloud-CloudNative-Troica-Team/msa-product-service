package dev.ktcloud.black.product.adapter.infrastructure.jpa.repository

import dev.ktcloud.black.product.adapter.infrastructure.jpa.ProductMapper
import dev.ktcloud.black.product.application.port.outbound.ProductCommandOutboundPort
import dev.ktcloud.black.product.domain.entity.ProductDomainEntity
import org.springframework.stereotype.Component

@Component
class ProductPostgresqlCommandRepository(
    private val repository: ProductPostgresqlRepository,
    private val productMapper: ProductMapper
): ProductCommandOutboundPort {
    override fun save(productDomainEntity: ProductDomainEntity): ProductDomainEntity {
        val productOrmEntity = productMapper.toOrmEntity(productDomainEntity)

        val saved = repository.save(productOrmEntity)

        return productMapper.toDomainEntity(saved)
    }
}