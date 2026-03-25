package com.whatap.user

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class UserApplicationTest {
    @Test
    fun `Spring 컨텍스트가 정상적으로 로드된다`() {
    }
}
