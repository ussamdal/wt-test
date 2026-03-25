package com.whatap.user.application.service

import com.whatap.user.adapter.outbound.persistence.UserFileLoader
import com.whatap.user.domain.model.User
import com.whatap.user.domain.port.inbound.PageResult
import com.whatap.user.domain.port.inbound.ParseErrorResult
import com.whatap.user.domain.port.inbound.ReloadResult
import com.whatap.user.domain.port.inbound.UserQueryUseCase
import com.whatap.user.domain.port.outbound.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Service
import kotlin.math.ceil
import kotlin.math.max

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userFileLoader: UserFileLoader,
) : UserQueryUseCase,
    ApplicationRunner {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    override fun run(args: ApplicationArguments?) {
        logger.info("Loading user data from file on startup...")
        reload()
    }

    override fun findAll(
        page: Int,
        size: Int,
    ): PageResult<User> {
        val safePage = max(0, page)
        val safeSize = max(1, size)

        val allUsers = userRepository.findAll()
        val totalElements = allUsers.size
        val totalPages = if (totalElements == 0) 0 else ceil(totalElements.toDouble() / safeSize).toInt()

        val fromIndex = (safePage * safeSize).coerceAtMost(totalElements)
        val toIndex = ((safePage + 1) * safeSize).coerceAtMost(totalElements)
        val content = allUsers.subList(fromIndex, toIndex)

        return PageResult(
            content = content,
            page = safePage,
            size = safeSize,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }

    override fun findById(id: Int): User? = userRepository.findById(id)

    override fun searchByName(name: String): List<User> = userRepository.searchByName(name)

    override fun getParseErrors(): List<ParseErrorResult> = userRepository.getParseErrors()

    override fun reload(): ReloadResult {
        val result = userFileLoader.load()
        userRepository.saveAll(result.users, result.errors)
        logger.info("Reloaded: {} users, {} errors", result.users.size, result.errors.size)
        return ReloadResult(
            loadedCount = result.users.size,
            errorCount = result.errors.size,
        )
    }
}
