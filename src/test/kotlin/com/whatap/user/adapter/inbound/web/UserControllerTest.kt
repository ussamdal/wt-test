package com.whatap.user.adapter.inbound.web

import com.whatap.user.domain.model.User
import com.whatap.user.domain.port.inbound.PageResult
import com.whatap.user.domain.port.inbound.ParseErrorResult
import com.whatap.user.domain.port.inbound.ReloadResult
import com.whatap.user.domain.port.inbound.UserQueryUseCase
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userQueryUseCase: UserQueryUseCase

    private val testUsers =
        listOf(
            User(id = 1001, name = "김다나", age = 23, gender = "M", salary = 30_000_000),
            User(id = 1002, name = "김나나", age = 24, gender = "F", salary = 31_000_000),
        )

    @Test
    fun `GET api users 는 페이징된 유저 목록을 반환한다`() {
        `when`(userQueryUseCase.findAll(0, 10)).thenReturn(
            PageResult(
                content = testUsers,
                page = 0,
                size = 10,
                totalElements = 2,
                totalPages = 1,
            ),
        )

        mockMvc
            .perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(1001))
            .andExpect(jsonPath("$.content[0].name").value("김다나"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1))
    }

    @Test
    fun `GET api users 는 page와 size 파라미터를 지원한다`() {
        `when`(userQueryUseCase.findAll(1, 5)).thenReturn(
            PageResult(
                content = emptyList(),
                page = 1,
                size = 5,
                totalElements = 2,
                totalPages = 1,
            ),
        )

        mockMvc
            .perform(get("/api/users?page=1&size=5"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.size").value(5))
    }

    @Test
    fun `GET api users id 는 존재하는 유저를 반환한다`() {
        `when`(userQueryUseCase.findById(1001)).thenReturn(testUsers[0])

        mockMvc
            .perform(get("/api/users/1001"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1001))
            .andExpect(jsonPath("$.name").value("김다나"))
            .andExpect(jsonPath("$.age").value(23))
            .andExpect(jsonPath("$.gender").value("M"))
            .andExpect(jsonPath("$.salary").value(30_000_000))
    }

    @Test
    fun `GET api users id 는 존재하지 않으면 404를 반환한다`() {
        `when`(userQueryUseCase.findById(9999)).thenReturn(null)

        mockMvc
            .perform(get("/api/users/9999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET api users search 는 이름으로 검색한 결과를 반환한다`() {
        `when`(userQueryUseCase.searchByName("김")).thenReturn(testUsers)

        mockMvc
            .perform(get("/api/users/search?name=김"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("김다나"))
    }

    @Test
    fun `GET api users errors 는 파싱 에러 목록을 반환한다`() {
        val errors =
            listOf(
                ParseErrorResult(line = 16, rawData = "1016 , 바사삭 , 38 , F , 45_OOO_000", reason = "Invalid salary"),
            )
        `when`(userQueryUseCase.getParseErrors()).thenReturn(errors)

        mockMvc
            .perform(get("/api/users/errors"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].line").value(16))
            .andExpect(jsonPath("$[0].rawData").value("1016 , 바사삭 , 38 , F , 45_OOO_000"))
            .andExpect(jsonPath("$[0].reason").value("Invalid salary"))
    }

    @Test
    fun `POST api users reload 는 리로드 결과를 반환한다`() {
        `when`(userQueryUseCase.reload()).thenReturn(ReloadResult(loadedCount = 37, errorCount = 3))

        mockMvc
            .perform(post("/api/users/reload"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.loadedCount").value(37))
            .andExpect(jsonPath("$.errorCount").value(3))
    }

    @Test
    fun `GET api users id 에 문자열 전달 시 400을 반환한다`() {
        mockMvc
            .perform(get("/api/users/abc"))
            .andExpect(status().isBadRequest)
    }
}
