package com.example.demo1

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(val value: String)

object ApiConfig {
    const val LAB_URL = "http://127.0.0.1:4444/TransferSimulator"
    const val INTERNET_URL = "http://prb.sylas.ru/TransferSimulator"

    var useLabAddress: Boolean = true
    val baseUrl: String get() = if (useLabAddress) LAB_URL else INTERNET_URL
}