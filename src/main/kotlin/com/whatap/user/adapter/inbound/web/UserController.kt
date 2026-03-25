package com.whatap.user.adapter.inbound.web

import com.whatap.user.adapter.inbound.web.dto.PageResponse
import com.whatap.user.adapter.inbound.web.dto.ReloadResponse
import com.whatap.user.adapter.inbound.web.dto.UserResponse
import com.whatap.user.domain.port.inbound.ParseErrorResult
import com.whatap.user.domain.port.inbound.UserQueryUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userQueryUseCase: UserQueryUseCase,
) {
    @GetMapping
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<PageResponse<UserResponse>> {
        val result = userQueryUseCase.findAll(page, size)
        val response =
            PageResponse(
                content = result.content.map { UserResponse.from(it) },
                page = result.page,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
            )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getUserById(
        @PathVariable id: Int,
    ): ResponseEntity<UserResponse> {
        val user = userQueryUseCase.findById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(UserResponse.from(user))
    }

    @GetMapping("/search")
    fun searchUsers(
        @RequestParam name: String,
    ): ResponseEntity<List<UserResponse>> {
        val users = userQueryUseCase.searchByName(name)
        return ResponseEntity.ok(users.map { UserResponse.from(it) })
    }

    @GetMapping("/errors")
    fun getParseErrors(): ResponseEntity<List<ParseErrorResult>> {
        val errors = userQueryUseCase.getParseErrors()
        return ResponseEntity.ok(errors)
    }

    @PostMapping("/reload")
    fun reload(): ResponseEntity<ReloadResponse> {
        val result = userQueryUseCase.reload()
        return ResponseEntity.ok(
            ReloadResponse(
                loadedCount = result.loadedCount,
                errorCount = result.errorCount,
            ),
        )
    }
}
