package com.example.hrdepartment.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hrdepartment.R
import com.example.hrdepartment.VacancyAdapter // (VA) - Вы тоже меняете под Retrofit
import com.example.hrdepartment.data.models.VacancyDto
import com.example.hrdepartment.network.ApiClient
import com.example.hrdepartment.ui.vacancies.AddVacancyActivity
import com.example.hrdepartment.ui.vacancies.VacancyDetailsActivity
import kotlinx.coroutines.launch

class VacanciesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addVacancyBtn: Button
    private lateinit var vacancyTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vacancies, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vacancyTitle = view.findViewById(R.id.tv_vacancy_title)
        recyclerView = view.findViewById(R.id.rv_vacancies)
        addVacancyBtn = view.findViewById(R.id.btn_add_vacancy)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadVacancies()

        addVacancyBtn.setOnClickListener {
            val intent = Intent(requireContext(), AddVacancyActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadVacancies()
    }

    private fun loadVacancies() {
        lifecycleScope.launch {
            try {
                // Получаем экземпляр ApiService
                val apiService = ApiClient.getApiService(requireContext())

                // Загружаем список вакансий
                val vacancies: List<VacancyDto> = apiService.getVacancies()

                // Устанавливаем адаптер для RecyclerView
                val adapter = VacancyAdapter(vacancies) { vacancy ->
                    val intent = Intent(requireContext(), VacancyDetailsActivity::class.java)
                    intent.putExtra("vacancy_id", vacancy.id)
                    startActivity(intent)
                }
                recyclerView.adapter = adapter
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error loading vacancies: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
