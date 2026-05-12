package dev.ktcloud.black.product.application.port.outbound

import dev.ktcloud.black.product.domain.entity.ProductDomainEntity

interface ProductCommandOutboundPort {
    fun save(productDomainEntity: ProductDomainEntity): ProductDomainEntity
}