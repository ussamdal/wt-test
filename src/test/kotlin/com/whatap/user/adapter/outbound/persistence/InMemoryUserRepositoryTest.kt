package com.whatap.user.adapter.outbound.persistence

import com.whatap.user.domain.model.User
import com.whatap.user.domain.port.inbound.ParseErrorResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemoryUserRepositoryTest {
    private lateinit var repository: InMemoryUserRepository

    private val testUsers =
        listOf(
            User(id = 1001, name = "김다나", age = 23, gender = "M", salary = 30_000_000),
            User(id = 1002, name = "김나나", age = 24, gender = "F", salary = 31_000_000),
            User(id = 1003, name = "이사사", age = 27, gender = "M", salary = 34_000_000),
        )

    private val testErrors =
        listOf(
            ParseErrorResult(line = 16, rawData = "1016 , 바사삭 , 38 , F , 45_OOO_000", reason = "Invalid salary"),
        )

    @BeforeEach
    fun setUp() {
        repository = InMemoryUserRepository()
        repository.saveAll(testUsers, testErrors)
    }

    @Test
    fun `findAll은 ID순으로 정렬된 전체 목록을 반환한다`() {
        val result = repository.findAll()

        assertEquals(3, result.size)
        assertEquals(1001, result[0].id)
        assertEquals(1002, result[1].id)
        assertEquals(1003, result[2].id)
    }

    @Test
    fun `findById로 존재하는 ID를 조회하면 User를 반환한다`() {
        val user = repository.findById(1001)

        assertEquals("김다나", user?.name)
    }

    @Test
    fun `findById로 존재하지 않는 ID를 조회하면 null을 반환한다`() {
        val user = repository.findById(9999)

        assertNull(user)
    }

    @Test
    fun `searchByName으로 이름에 포함된 문자열을 검색한다`() {
        val result = repository.searchByName("김")

        assertEquals(2, result.size)
        assertEquals(1001, result[0].id)
        assertEquals(1002, result[1].id)
    }

    @Test
    fun `searchByName으로 일치하지 않는 문자열을 검색하면 빈 목록을 반환한다`() {
        val result = repository.searchByName("없는이름")

        assertEquals(0, result.size)
    }

    @Test
    fun `getParseErrors는 저장된 파싱 에러를 반환한다`() {
        val errors = repository.getParseErrors()

        assertEquals(1, errors.size)
        assertEquals(16, errors[0].line)
    }

    @Test
    fun `saveAll 호출 시 기존 데이터가 교체된다`() {
        val newUsers = listOf(User(id = 2001, name = "새유저", age = 30, gender = "M", salary = 50_000_000))
        val newErrors = emptyList<ParseErrorResult>()

        repository.saveAll(newUsers, newErrors)

        assertEquals(1, repository.findAll().size)
        assertEquals("새유저", repository.findById(2001)?.name)
        assertNull(repository.findById(1001))
        assertEquals(0, repository.getParseErrors().size)
    }
}
