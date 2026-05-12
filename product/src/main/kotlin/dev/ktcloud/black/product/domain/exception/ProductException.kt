package dev.ktcloud.black.product.domain.exception

import dev.ktcloud.black.common.exception.CustomException
import org.springframework.http.HttpStatus

sealed class ProductException {
    class NoSuchProductException(message: String? = null, e: Throwable? = null): CustomException(
        "001",
        message ?: "該当の商品は存在しません",
        HttpStatus.NOT_FOUND,
        e
    )
}