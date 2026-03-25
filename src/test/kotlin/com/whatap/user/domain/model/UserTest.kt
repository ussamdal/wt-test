package com.whatap.user.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class UserTest {
    @Test
    fun `User 생성 시 모든 필드가 올바르게 설정된다`() {
        val user = User(id = 1001, name = "김다나", age = 23, gender = "M", salary = 30_000_000)

        assertEquals(1001, user.id)
        assertEquals("김다나", user.name)
        assertEquals(23, user.age)
        assertEquals("M", user.gender)
        assertEquals(30_000_000, user.salary)
    }

    @Test
    fun `동일한 필드를 가진 User는 동등하다`() {
        val user1 = User(id = 1001, name = "김다나", age = 23, gender = "M", salary = 30_000_000)
        val user2 = User(id = 1001, name = "김다나", age = 23, gender = "M", salary = 30_000_000)

        assertEquals(user1, user2)
        assertEquals(user1.hashCode(), user2.hashCode())
    }

    @Test
    fun `다른 필드를 가진 User는 동등하지 않다`() {
        val user1 = User(id = 1001, name = "김다나", age = 23, gender = "M", salary = 30_000_000)
        val user2 = User(id = 1002, name = "김나나", age = 24, gender = "F", salary = 31_000_000)

        assertNotEquals(user1, user2)
    }

    @Test
    fun `User의 copy를 통해 특정 필드만 변경할 수 있다`() {
        val original = User(id = 1001, name = "김다나", age = 23, gender = "M", salary = 30_000_000)
        val modified = original.copy(salary = 35_000_000)

        assertEquals(1001, modified.id)
        assertEquals("김다나", modified.name)
        assertEquals(35_000_000, modified.salary)
    }
}
