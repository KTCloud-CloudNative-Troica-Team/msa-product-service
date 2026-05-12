package dev.ktcloud.black.product.application.port.inbound

import dev.ktcloud.black.product.domain.entity.ProductDomainEntity

interface CreateProductCommand {
    fun createProduct(command: In): Out

    data class In(
        val name: String,
        val description: String,
        val price: Int,
    )

    data class Out(
        val id: String,
        val name: String,
        val description: String,
        val price: Int,
    ) {
        companion object {
            fun from(product: ProductDomainEntity): Out {
                return Out(
                    id = product.id.toString(),
                    name = product.name,
                    description = product.description,
                    price = product.price,
                )
            }
        }
    }
}