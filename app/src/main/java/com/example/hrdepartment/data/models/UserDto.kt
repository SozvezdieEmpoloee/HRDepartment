// app/src/main/java/com/example/hrdepartment/data/models/UserDto.kt

package com.example.hrdepartment.data.models

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int? = null,
    val name: String,
    val email: String,
    val password: String,
    val phone: String?,
    val photo_url: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String
)
data class RegisterResponse(
    val user_id: Int
)
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String,
    val photo_url: String?,
    val special_code: String
)