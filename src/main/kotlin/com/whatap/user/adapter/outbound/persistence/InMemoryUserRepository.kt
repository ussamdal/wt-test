package com.whatap.user.adapter.outbound.persistence

import com.whatap.user.domain.model.User
import com.whatap.user.domain.port.inbound.ParseErrorResult
import com.whatap.user.domain.port.outbound.UserRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryUserRepository : UserRepository {
    private val userStore = ConcurrentHashMap<Int, User>()

    @Volatile
    private var parseErrors: List<ParseErrorResult> = emptyList()

    override fun findAll(): List<User> = userStore.values.sortedBy { it.id }

    override fun findById(id: Int): User? = userStore[id]

    override fun searchByName(name: String): List<User> =
        userStore.values
            .filter { it.name.contains(name) }
            .sortedBy { it.id }

    override fun saveAll(
        users: List<User>,
        errors: List<ParseErrorResult>,
    ) {
        userStore.clear()
        users.forEach { userStore[it.id] = it }
        parseErrors = errors
    }

    override fun getParseErrors(): List<ParseErrorResult> = parseErrors
}
