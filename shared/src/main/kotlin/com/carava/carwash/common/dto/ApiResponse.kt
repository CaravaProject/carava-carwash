package com.carava.carwash.common.dto

data class ApiResponse<T> (
    val isSuccess: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null
) {
    companion object {
        fun <T> success(data: T? = null, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                isSuccess = true,
                data = data,
                message = message
            )
        }

        fun error(errorCode: String? = null, message: String? = null): ApiResponse<Nothing> {
            return ApiResponse(
                isSuccess = false,
                errorCode = errorCode,
                message = message,
                data = null
            )
        }
    }
}
