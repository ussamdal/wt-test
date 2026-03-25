package com.whatap.user.adapter.outbound.persistence

import com.whatap.user.domain.model.User
import com.whatap.user.domain.port.inbound.ParseErrorResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
class UserFileLoader(
    @Value("\${app.user-file-path}")
    private val userFileResource: Resource,
) {
    private val logger = LoggerFactory.getLogger(UserFileLoader::class.java)

    data class LoadResult(
        val users: List<User>,
        val errors: List<ParseErrorResult>,
    )

    fun load(): LoadResult {
        val users = mutableListOf<User>()
        val errors = mutableListOf<ParseErrorResult>()

        val lines = userFileResource.inputStream.bufferedReader().readLines()

        lines.forEachIndexed { index, line ->
            val lineNumber = index + 1
            val trimmedLine = line.trim()

            if (trimmedLine.isBlank()) {
                return@forEachIndexed
            }

            try {
                val user = parseLine(trimmedLine)
                users.add(user)
            } catch (e: Exception) {
                val errorResult =
                    ParseErrorResult(
                        line = lineNumber,
                        rawData = trimmedLine,
                        reason = e.message ?: "Unknown error",
                    )
                errors.add(errorResult)
                logger.warn(
                    "Failed to parse line {}: '{}' - {}",
                    lineNumber,
                    trimmedLine,
                    e.message,
                )
            }
        }

        logger.info("Loaded {} users, {} parse errors", users.size, errors.size)
        return LoadResult(users = users, errors = errors)
    }

    private fun parseLine(line: String): User {
        val parts = line.split(",").map { it.trim() }

        if (parts.size != 5) {
            throw IllegalArgumentException(
                "Expected 5 fields but got ${parts.size}",
            )
        }

        val id =
            parts[0].toIntOrNull()
                ?: throw NumberFormatException("Invalid ID: '${parts[0]}'")

        val name = parts[1]
        if (name.isBlank()) {
            throw IllegalArgumentException("Name is blank")
        }

        val age =
            parts[2].toIntOrNull()
                ?: throw NumberFormatException("Invalid age: '${parts[2]}'")

        val gender = parts[3]
        if (gender !in listOf("M", "F")) {
            throw IllegalArgumentException("Invalid gender: '${parts[3]}'")
        }

        val salaryStr = parts[4].replace("_", "")
        val salary =
            salaryStr.toIntOrNull()
                ?: throw NumberFormatException("Invalid salary: '${parts[4]}'")

        return User(
            id = id,
            name = name,
            age = age,
            gender = gender,
            salary = salary,
        )
    }
}
