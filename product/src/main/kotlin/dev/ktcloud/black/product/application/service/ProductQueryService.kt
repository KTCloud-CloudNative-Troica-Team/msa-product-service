package dev.ktcloud.black.product.application.service

import dev.ktcloud.black.product.application.port.inbound.FetchAllProductsQuery
import dev.ktcloud.black.product.application.port.inbound.FetchProductQuery
import dev.ktcloud.black.product.application.port.outbound.ProductQueryOutboundPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductQueryService(
    private val productQueryOutboundPort: ProductQueryOutboundPort
): FetchAllProductsQuery, FetchProductQuery {
    @Transactional(readOnly = true)
    override fun fetchAll(): List<FetchAllProductsQuery.Out> {
        val products = productQueryOutboundPort.fetchAll()

        return products.map { FetchAllProductsQuery.Out(
            id = it.id.toString(),
            name = it.name,
            description = it.description,
            price = it.price,
        )}
    }

    @Transactional(readOnly = true)
    override fun fetch(query: FetchProductQuery.In): FetchProductQuery.Out {
        val product = productQueryOutboundPort.fetch(query.id)

        return FetchProductQuery.Out(
            id = product.id.toString(),
            name = product.name,
            description = product.description,
            price = product.price,
        )
    }
}