package com.example.hrdepartment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hrdepartment.data.models.VacancyDto

class VacancyAdapter(
    private val vacancies: List<VacancyDto>,
    private val onItemClick: (VacancyDto) -> Unit
) : RecyclerView.Adapter<VacancyAdapter.VacancyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacancyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vacancy_item, parent, false)
        return VacancyViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: VacancyViewHolder, position: Int) {
        holder.bind(vacancies[position])
    }

    override fun getItemCount(): Int = vacancies.size

    class VacancyViewHolder(
        itemView: View,
        private val onItemClick: (VacancyDto) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.job_title)
        private val salary: TextView = itemView.findViewById(R.id.job_salary)
        private val experience: TextView = itemView.findViewById(R.id.job_experience)
        private val detailsButton: ImageButton = itemView.findViewById(R.id.button_job_details)

        fun bind(vacancy: VacancyDto) {
            title.text = vacancy.title
            salary.text = "Salary: ${vacancy.salary}"
            experience.text = "Experience: ${vacancy.experience}"

            detailsButton.setOnClickListener {
                onItemClick(vacancy)
            }
            // Или itemView.setOnClickListener { onItemClick(vacancy) }
        }
    }
}