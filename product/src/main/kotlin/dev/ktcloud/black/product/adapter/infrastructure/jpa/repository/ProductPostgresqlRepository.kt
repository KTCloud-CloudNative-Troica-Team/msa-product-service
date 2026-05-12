package dev.ktcloud.black.product.adapter.infrastructure.jpa.repository

import dev.ktcloud.black.product.adapter.infrastructure.jpa.entity.Product
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProductPostgresqlRepository: CrudRepository<Product, UUID> {
}