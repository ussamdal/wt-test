package com.whatap.user.adapter.outbound.persistence

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class UserFileLoaderTest {
    private fun createLoader(resourcePath: String): UserFileLoader = UserFileLoader(ClassPathResource(resourcePath))

    @Test
    fun `정상 데이터만 있는 파일을 파싱하면 모두 성공한다`() {
        val loader = createLoader("user-valid.txt")
        val result = loader.load()

        assertEquals(3, result.users.size)
        assertEquals(0, result.errors.size)
        assertEquals(1001, result.users[0].id)
        assertEquals("김다나", result.users[0].name)
        assertEquals(23, result.users[0].age)
        assertEquals("M", result.users[0].gender)
        assertEquals(30_000_000, result.users[0].salary)
    }

    @Test
    fun `오류 데이터가 포함된 파일을 파싱하면 에러가 감지된다`() {
        val loader = createLoader("user-with-errors.txt")
        val result = loader.load()

        assertEquals(1, result.users.size)
        assertEquals(3, result.errors.size)

        val errorLines = result.errors.map { it.line }
        assertTrue(errorLines.contains(2))
        assertTrue(errorLines.contains(3))
        assertTrue(errorLines.contains(4))
    }

    @Test
    fun `연봉에 문자가 포함된 경우 파싱 에러가 발생한다`() {
        val loader = createLoader("user-with-errors.txt")
        val result = loader.load()

        val salaryError = result.errors.find { it.line == 2 }
        assertTrue(salaryError != null)
        assertTrue(salaryError!!.reason.contains("salary", ignoreCase = true))
    }

    @Test
    fun `이중 콤마로 필드 개수가 맞지 않으면 파싱 에러가 발생한다`() {
        val loader = createLoader("user-with-errors.txt")
        val result = loader.load()

        val fieldError = result.errors.find { it.line == 3 }
        assertTrue(fieldError != null)
        assertTrue(fieldError!!.reason.contains("5 fields", ignoreCase = true))
    }

    @Test
    fun `빈 파일을 파싱하면 빈 결과를 반환한다`() {
        val loader = createLoader("user-empty.txt")
        val result = loader.load()

        assertEquals(0, result.users.size)
        assertEquals(0, result.errors.size)
    }

    @Test
    fun `연봉의 언더스코어가 정상적으로 제거되어 파싱된다`() {
        val loader = createLoader("user-valid.txt")
        val result = loader.load()

        assertEquals(30_000_000, result.users[0].salary)
        assertEquals(31_000_000, result.users[1].salary)
    }
}
