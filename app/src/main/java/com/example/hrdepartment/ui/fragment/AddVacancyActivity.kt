package com.example.hrdepartment.ui.vacancies

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hrdepartment.R
import com.example.hrdepartment.data.models.VacancyDto
import com.example.hrdepartment.network.ApiClient
import kotlinx.coroutines.launch

class AddVacancyActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etSalary: EditText
    private lateinit var etExperience: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_vacancy)

        // Инициализация View
        etTitle = findViewById(R.id.et_vacancy_title)
        etSalary = findViewById(R.id.et_vacancy_salary)
        etExperience = findViewById(R.id.et_vacancy_experience)
        etDescription = findViewById(R.id.et_vacancy_description)
        btnSave = findViewById(R.id.btn_save_new_vacancy)

        // Слушатель кнопки сохранения
        btnSave.setOnClickListener {
            saveVacancy()
        }
    }

    private fun saveVacancy() {
        val title = etTitle.text.toString().trim()
        val salary = etSalary.text.toString().trim()
        val experience = etExperience.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (title.isEmpty() || salary.isEmpty() || experience.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Асинхронное сохранение вакансии
        lifecycleScope.launch {
            try {
                val newVacancy = VacancyDto(
                    title = title,
                    salary = salary,
                    experience = experience,
                    description = description,
                )

                // Получаем экземпляр ApiService
                val apiService = ApiClient.getApiService(applicationContext)

                // Создаём вакансию через API
                val created = apiService.createVacancy(newVacancy)

                // Показываем уведомление об успешном добавлении
                Toast.makeText(applicationContext, "Vacancy added!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
