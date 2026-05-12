package dev.ktcloud.black.product.application.port.outbound

import dev.ktcloud.black.product.domain.entity.ProductDomainEntity

interface ProductQueryOutboundPort {
    fun fetch(id: String): ProductDomainEntity
    fun fetchAll(): List<ProductDomainEntity>
}