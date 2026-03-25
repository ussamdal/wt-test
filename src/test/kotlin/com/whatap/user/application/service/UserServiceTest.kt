package com.whatap.user.application.service

import com.whatap.user.adapter.outbound.persistence.UserFileLoader
import com.whatap.user.domain.model.User
import com.whatap.user.domain.port.inbound.ParseErrorResult
import com.whatap.user.domain.port.outbound.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class UserServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var userFileLoader: UserFileLoader
    private lateinit var userService: UserService

    private val testUsers =
        listOf(
            User(id = 1001, name = "김다나", age = 23, gender = "M", salary = 30_000_000),
            User(id = 1002, name = "김나나", age = 24, gender = "F", salary = 31_000_000),
            User(id = 1003, name = "이사사", age = 27, gender = "M", salary = 34_000_000),
            User(id = 1004, name = "김다다", age = 25, gender = "F", salary = 32_000_000),
            User(id = 1005, name = "김라라", age = 26, gender = "M", salary = 33_000_000),
        )

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        userFileLoader = mock(UserFileLoader::class.java)
        userService = UserService(userRepository, userFileLoader)
    }

    @Test
    fun `findAll은 페이징된 결과를 반환한다`() {
        `when`(userRepository.findAll()).thenReturn(testUsers)

        val result = userService.findAll(page = 0, size = 2)

        assertEquals(2, result.content.size)
        assertEquals(0, result.page)
        assertEquals(2, result.size)
        assertEquals(5, result.totalElements)
        assertEquals(3, result.totalPages)
        assertEquals(1001, result.content[0].id)
        assertEquals(1002, result.content[1].id)
    }

    @Test
    fun `findAll 두번째 페이지를 요청하면 올바른 데이터를 반환한다`() {
        `when`(userRepository.findAll()).thenReturn(testUsers)

        val result = userService.findAll(page = 1, size = 2)

        assertEquals(2, result.content.size)
        assertEquals(1003, result.content[0].id)
        assertEquals(1004, result.content[1].id)
    }

    @Test
    fun `findAll 마지막 페이지는 남은 데이터만 반환한다`() {
        `when`(userRepository.findAll()).thenReturn(testUsers)

        val result = userService.findAll(page = 2, size = 2)

        assertEquals(1, result.content.size)
        assertEquals(1005, result.content[0].id)
    }

    @Test
    fun `findAll 범위를 초과하는 페이지를 요청하면 빈 목록을 반환한다`() {
        `when`(userRepository.findAll()).thenReturn(testUsers)

        val result = userService.findAll(page = 10, size = 2)

        assertEquals(0, result.content.size)
        assertEquals(5, result.totalElements)
    }

    @Test
    fun `findAll 음수 page는 0으로 보정된다`() {
        `when`(userRepository.findAll()).thenReturn(testUsers)

        val result = userService.findAll(page = -1, size = 2)

        assertEquals(0, result.page)
        assertEquals(2, result.content.size)
    }

    @Test
    fun `findAll 음수 size는 1로 보정된다`() {
        `when`(userRepository.findAll()).thenReturn(testUsers)

        val result = userService.findAll(page = 0, size = -5)

        assertEquals(1, result.size)
        assertEquals(1, result.content.size)
    }

    @Test
    fun `findById로 존재하는 User를 조회한다`() {
        `when`(userRepository.findById(1001)).thenReturn(testUsers[0])

        val user = userService.findById(1001)

        assertEquals("김다나", user?.name)
    }

    @Test
    fun `findById로 존재하지 않는 User를 조회하면 null을 반환한다`() {
        `when`(userRepository.findById(9999)).thenReturn(null)

        val user = userService.findById(9999)

        assertNull(user)
    }

    @Test
    fun `searchByName으로 이름을 검색한다`() {
        val matchedUsers = testUsers.filter { it.name.contains("김") }
        `when`(userRepository.searchByName("김")).thenReturn(matchedUsers)

        val result = userService.searchByName("김")

        assertEquals(4, result.size)
    }

    @Test
    fun `getParseErrors는 파싱 에러 목록을 반환한다`() {
        val errors =
            listOf(
                ParseErrorResult(line = 16, rawData = "raw", reason = "error"),
            )
        `when`(userRepository.getParseErrors()).thenReturn(errors)

        val result = userService.getParseErrors()

        assertEquals(1, result.size)
        assertEquals(16, result[0].line)
    }

    @Test
    fun `reload는 파일을 다시 읽고 저장소를 갱신한다`() {
        val loadResult =
            UserFileLoader.LoadResult(
                users = testUsers.take(3),
                errors =
                    listOf(
                        ParseErrorResult(line = 1, rawData = "bad", reason = "err"),
                    ),
            )
        `when`(userFileLoader.load()).thenReturn(loadResult)

        val result = userService.reload()

        assertEquals(3, result.loadedCount)
        assertEquals(1, result.errorCount)
        verify(userRepository).saveAll(loadResult.users, loadResult.errors)
    }
}
