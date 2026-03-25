package com.whatap.user.domain.port.outbound

import com.whatap.user.domain.model.User
import com.whatap.user.domain.port.inbound.ParseErrorResult

interface UserRepository {
    fun findAll(): List<User>

    fun findById(id: Int): User?

    fun searchByName(name: String): List<User>

    fun saveAll(
        users: List<User>,
        errors: List<ParseErrorResult>,
    )

    fun getParseErrors(): List<ParseErrorResult>
}
