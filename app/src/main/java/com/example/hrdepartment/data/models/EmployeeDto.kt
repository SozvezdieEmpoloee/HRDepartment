// app/src/main/java/com/example/hrdepartment/data/models/EmployeeDto.kt

package com.example.hrdepartment.data.models

data class EmployeeDto(
    val id: Int? = null,
    val first_name: String,
    val last_name: String,
    val email: String?,
    val phone: String?,
    val photo_url: String?,
    val salary: Double?,
    val gender: String?,
    val age: Int?,
    val nationality: String?,
    val place_of_residence: String?,
    val characteristics: String?,
    val created_at: String?,
    val updated_at: String?
)
