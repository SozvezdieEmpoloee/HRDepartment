// app/src/main/java/com/example/hrdepartment/data/models/VacancyApplicantDto.kt

package com.example.hrdepartment.data.models

import com.google.gson.annotations.SerializedName

data class VacancyApplicantDto(
    val id: Int? = null,
    @SerializedName("vacancy")
    val vacancyId: Int, // ID вакансии, к которой относится соискатель

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("contact_email")
    val contactEmail: String?,

    @SerializedName("social_link")
    val socialLink: String?,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)