package dev.ktcloud.black.product.service.adapter.web.inbound

import dev.ktcloud.black.product.service.adapter.presentation.web.inbound.grpc.CreateProductRequest
import dev.ktcloud.black.product.service.adapter.presentation.web.inbound.grpc.Empty
import dev.ktcloud.black.product.service.adapter.presentation.web.inbound.grpc.FetchAllProductsResponse
import dev.ktcloud.black.product.service.adapter.presentation.web.inbound.grpc.FetchProductRequest
import dev.ktcloud.black.product.service.adapter.presentation.web.inbound.grpc.ProductResponseDto
import dev.ktcloud.black.product.service.adapter.presentation.web.inbound.grpc.ProductServiceGrpcKt

abstract class ProductGrpcController: ProductServiceGrpcKt.ProductServiceCoroutineImplBase() {
    abstract override suspend fun createProduct(request: CreateProductRequest): ProductResponseDto

    abstract override suspend fun fetchProduct(request: FetchProductRequest): ProductResponseDto

    abstract override suspend fun fetchAll(request: Empty): FetchAllProductsResponse
}