// app/src/main/java/com/example/hrdepartment/data/models/VacancyDto.kt

package com.example.hrdepartment.data.models

import com.google.gson.annotations.SerializedName


data class VacancyDto(
    val id: Int? = null,
    val title: String,
    val salary: String,
    val experience: String,
    val description: String,
    val photo_url: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,

    // Теперь это массив объектов
    val applicants: List<VacancyApplicantDto>? = emptyList()
)