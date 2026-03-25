package com.whatap.user.adapter.inbound.web.dto

import com.whatap.user.domain.model.User

data class UserResponse(
    val id: Int,
    val name: String,
    val age: Int,
    val gender: String,
    val salary: Int,
) {
    companion object {
        fun from(user: User): UserResponse =
            UserResponse(
                id = user.id,
                name = user.name,
                age = user.age,
                gender = user.gender,
                salary = user.salary,
            )
    }
}
