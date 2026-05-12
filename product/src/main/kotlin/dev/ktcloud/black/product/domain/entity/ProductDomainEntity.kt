package dev.ktcloud.black.product.domain.entity

import java.util.UUID

data class ProductDomainEntity(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val price: Int,
)