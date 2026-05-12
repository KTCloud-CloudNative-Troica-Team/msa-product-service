package dev.ktcloud.black.product.adapter.infrastructure.jpa

import dev.ktcloud.black.common.adapter.infrastructure.jpa.EntityMapper
import dev.ktcloud.black.product.adapter.infrastructure.jpa.entity.Product
import dev.ktcloud.black.product.domain.entity.ProductDomainEntity
import org.springframework.stereotype.Component

@Component
class ProductMapper: EntityMapper<Product, ProductDomainEntity> {
    override fun toOrmEntity(domainEntity: ProductDomainEntity): Product {
        return Product(
            id = domainEntity.id,
            name = domainEntity.name,
            description = domainEntity.description,
            price = domainEntity.price,
        )
    }

    override fun toDomainEntity(entity: Product): ProductDomainEntity {
        return ProductDomainEntity(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            price = entity.price,
        )
    }
}