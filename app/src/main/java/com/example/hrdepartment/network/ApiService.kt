// app/src/main/java/com/example/hrdepartment/network/ApiService.kt

package com.example.hrdepartment.network

import com.example.hrdepartment.data.models.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Body

data class TokenRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    val refresh: String,
    val access: String,
    val user_id: Int  // или Int? если нужно
)


data class LoginRequest(
    val email: String,
    val password: String
)

interface ApiService {

    // ------------------ Users Endpoints ------------------

    @GET("api/users/")
    suspend fun getUsers(): List<UserDto>

    // Создание нового пользователя
    @POST("api/users/")
    suspend fun createUser(@Body user: UserDto): Response<UserDto>

    @POST("api/register/")
    suspend fun registerUser(@Body user: RegisterRequest): Response<RegisterResponse>
    // Логин
    // Логин
    @POST("api/token/")
    suspend fun obtainToken(@Body loginRequest: LoginRequest): Response<TokenResponse>

    // Получение пользователя по ID
    @GET("api/users/{id}/")
    suspend fun getUserById(@Path("id") id: Int): UserDto

    // Обновление пользователя
    @PUT("api/users/{id}/")
    suspend fun updateUser(@Path("id") id: Int, @Body user: UserDto): Response<UserDto>


    // Удаление пользователя
    @DELETE("api/users/{id}/")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    // Смена пароля
    @POST("api/users/{id}/change_password/")
    suspend fun changePassword(
        @Path("id") id: Int,
        @Body request: ChangePasswordRequest
    ): Response<Unit>


    // ------------------ Vacancies Endpoints ------------------

    @GET("api/vacancies/")
    suspend fun getVacancies(): List<VacancyDto>

    @POST("api/vacancies/")
    suspend fun createVacancy(@Body vacancy: VacancyDto): VacancyDto

    @GET("api/vacancies/{id}/")
    suspend fun getVacancyById(@Path("id") id: Int): VacancyDto

    @PUT("api/vacancies/{id}/")
    suspend fun updateVacancy(@Path("id") id: Int, @Body vacancy: VacancyDto): VacancyDto

    @DELETE("api/vacancies/{id}/")
    suspend fun deleteVacancy(@Path("id") id: Int): Response<Unit>

    // ------------------ Vacancy Applicants Endpoints ------------------

    @GET("api/applicants/")
    suspend fun getApplicants(): List<VacancyApplicantDto>

    @POST("api/applicants/")
    suspend fun createApplicant(@Body applicant: VacancyApplicantDto): VacancyApplicantDto

    @GET("api/applicants/{id}/")
    suspend fun getApplicantById(@Path("id") id: Int): VacancyApplicantDto

    @PUT("api/applicants/{id}/")
    suspend fun updateApplicant(@Path("id") id: Int, @Body applicant: VacancyApplicantDto): VacancyApplicantDto

    @DELETE("api/applicants/{id}/")
    suspend fun deleteApplicant(@Path("id") id: Int)

    // ------------------ Employees Endpoints ------------------

    @GET("api/employees/")
    suspend fun getEmployees(): List<EmployeeDto>

    @GET("api/employees/{id}/")
    suspend fun getEmployeeById(@Path("id") id: Int): EmployeeDto

    @POST("api/employees/")
    suspend fun createEmployee(@Body employee: EmployeeDto): EmployeeDto

    @PUT("api/employees/{id}/")
    suspend fun updateEmployee(@Path("id") id: Int, @Body employee: EmployeeDto): EmployeeDto

    @DELETE("api/employees/{id}/")
    suspend fun deleteEmployee(@Path("id") id: Int): Response<Unit> // Изменено возвращаемое значение


    // ------------------ Token ------------------

    @POST("api/token/")
    suspend fun login(@Body loginRequest: LoginRequest): Response<TokenResponse>
}
