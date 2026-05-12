package dev.ktcloud.black.product.adapter.infrastructure.jpa.repository

import dev.ktcloud.black.product.adapter.infrastructure.jpa.ProductMapper
import dev.ktcloud.black.product.application.port.outbound.ProductQueryOutboundPort
import dev.ktcloud.black.product.domain.entity.ProductDomainEntity
import dev.ktcloud.black.product.domain.exception.ProductException
import org.springframework.stereotype.Component
import java.util.UUID


@Component
class ProductPostgresqlQueryRepository(
    private val repository: ProductPostgresqlRepository,
    private val productMapper: ProductMapper
): ProductQueryOutboundPort {
    override fun fetch(id: String): ProductDomainEntity {
        val optional = repository.findById(UUID.fromString(id))

        if (!optional.isPresent) throw ProductException.NoSuchProductException()

        return productMapper.toDomainEntity(optional.get())
    }

    override fun fetchAll(): List<ProductDomainEntity> {
        val productOrmEntities = repository.findAll()

        return productOrmEntities.map(productMapper::toDomainEntity)
    }
}