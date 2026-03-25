package com.whatap.user.domain.port.inbound

import com.whatap.user.domain.model.User

interface UserQueryUseCase {
    fun findAll(
        page: Int,
        size: Int,
    ): PageResult<User>

    fun findById(id: Int): User?

    fun searchByName(name: String): List<User>

    fun getParseErrors(): List<ParseErrorResult>

    fun reload(): ReloadResult
}

data class PageResult<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
)

data class ParseErrorResult(
    val line: Int,
    val rawData: String,
    val reason: String,
)

data class ReloadResult(
    val loadedCount: Int,
    val errorCount: Int,
)
