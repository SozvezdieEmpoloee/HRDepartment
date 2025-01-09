package com.example.hrdepartment.ui.vacancies

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hrdepartment.R
import com.example.hrdepartment.data.models.VacancyApplicantDto
import com.example.hrdepartment.data.models.VacancyDto
import com.example.hrdepartment.network.ApiClient
import kotlinx.coroutines.launch

class VacancyDetailsActivity : AppCompatActivity() {

    private var vacancyId: Int = -1
    private lateinit var tvTitle: TextView
    private lateinit var tvSalary: TextView
    private lateinit var tvExperience: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvResponded: TextView

    private lateinit var btnBack: ImageButton
    private lateinit var btnEditDescription: Button
    private lateinit var btnEditResponded: Button
    private lateinit var btnDelete: Button

    private lateinit var rvRespondedPeople: RecyclerView
    private lateinit var respondedPeopleAdapter: RespondedPeopleAdapter

    private var currentVacancy: VacancyDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vacancy_details)

        vacancyId = intent.getIntExtra("vacancy_id", -1)

        initViews()
        loadVacancyData()

        btnBack.setOnClickListener { finish() }

        btnEditDescription.setOnClickListener {
            showEditFieldDialog(
                title = "Edit Description",
                initialText = currentVacancy?.description ?: ""
            ) { newDesc ->
                updateVacancyDescription(newDesc)
            }
        }

        btnEditResponded.setOnClickListener {
            // Для добавления откликнувшихся
            showEditRespondedPeopleDialog()
        }

        btnDelete.setOnClickListener {
            deleteVacancy()
        }
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tv_details_title)
        tvSalary = findViewById(R.id.tv_details_salary)
        tvExperience = findViewById(R.id.tv_details_experience)
        tvDescription = findViewById(R.id.tv_details_description)
        tvResponded = findViewById(R.id.tv_responded_label)

        btnBack = findViewById(R.id.btn_back)
        btnEditDescription = findViewById(R.id.btn_edit_description)
        btnEditResponded = findViewById(R.id.btn_edit_responded)
        btnDelete = findViewById(R.id.btn_delete_vacancy)

        rvRespondedPeople = findViewById(R.id.rv_responded_people)
        rvRespondedPeople.layoutManager = LinearLayoutManager(this)
        respondedPeopleAdapter = RespondedPeopleAdapter(emptyList())
        rvRespondedPeople.adapter = respondedPeopleAdapter
    }


    private fun loadVacancyData() {
        if (vacancyId == -1) {
            Toast.makeText(this, "No vacancy ID passed", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(applicationContext)
                val vacancy = apiService.getVacancyById(vacancyId)
                currentVacancy = vacancy
                showVacancy(vacancy)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Error loading vacancy: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun showVacancy(vacancy: VacancyDto) {
        tvTitle.text = vacancy.title
        tvSalary.text = "Salary: ${vacancy.salary}"
        tvExperience.text = "Experience: ${vacancy.experience}"
        tvDescription.text = vacancy.description

        // обновляем список откликнувшихся
        respondedPeopleAdapter.updateList(vacancy.applicants.orEmpty())
    }

    private fun updateVacancyDescription(newDesc: String) {
        lifecycleScope.launch {
            try {
                currentVacancy?.let { old ->
                    val apiService = ApiClient.getApiService(applicationContext)
                    val updated = old.copy(description = newDesc)
                    val result = apiService.updateVacancy(old.id!!, updated)
                    showVacancy(result)
                    Toast.makeText(this@VacancyDetailsActivity, "Description updated", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@VacancyDetailsActivity, "Error updating description: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showEditRespondedPeopleDialog() {
        // Используем новый лейаут для диалога
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_responded_person, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_person_name)
        val etEmail = dialogView.findViewById<EditText>(R.id.et_person_email)
        val etLink = dialogView.findViewById<EditText>(R.id.et_person_link)

        AlertDialog.Builder(this)
            .setTitle("Add Responded Person")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val link = etLink.text.toString().trim()
                if (name.isNotEmpty()) {
                    addApplicantToVacancy(name, email, link)
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addApplicantToVacancy(name: String, contactEmail: String?, link: String?) {
        val vacancyId = currentVacancy?.id ?: return

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(applicationContext, requireAuth = false)
                val newApplicant = VacancyApplicantDto(
                    vacancyId = vacancyId,
                    fullName = name,
                    contactEmail = contactEmail,
                    socialLink = link
                )
                val created = apiService.createApplicant(newApplicant)
                // Успешно создали, теперь обновим UI
                val updatedVacancy = apiService.getVacancyById(vacancyId)
                currentVacancy = updatedVacancy
                showVacancy(updatedVacancy)
                Toast.makeText(this@VacancyDetailsActivity, "Applicant added", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@VacancyDetailsActivity, "Error adding applicant: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun deleteVacancy() {
        lifecycleScope.launch {
            try {
                currentVacancy?.let { vac ->
                    val apiService = ApiClient.getApiService(applicationContext)
                    val response = apiService.deleteVacancy(vac.id!!)
                    if (response.isSuccessful) {
                        Toast.makeText(this@VacancyDetailsActivity, "Vacancy deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@VacancyDetailsActivity, "Error deleting vacancy: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@VacancyDetailsActivity, "Error deleting vacancy: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showRemoveRespondedPersonDialog(position: Int) {
        // если нужно удалять
        Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show()
    }

    private fun showEditFieldDialog(title: String, initialText: String, onSave: (String) -> Unit) {
        val editText = EditText(this)
        editText.setText(initialText)

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(editText)
            .setPositiveButton("Save") { dialog, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    onSave(newText)
                } else {
                    Toast.makeText(this, "Field cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
class RespondedPeopleAdapter(
    private var respondedApplicants: List<VacancyApplicantDto>
) : RecyclerView.Adapter<RespondedPeopleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_responded_person, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = respondedApplicants.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(respondedApplicants[position])
    }

    fun updateList(newList: List<VacancyApplicantDto>) {
        respondedApplicants = newList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.tv_person_name)
        private val emailText: TextView = itemView.findViewById(R.id.tv_person_email)
        private val linkText: TextView = itemView.findViewById(R.id.tv_person_link)

        fun bind(applicant: VacancyApplicantDto) {
            nameText.text = applicant.fullName
            emailText.text = applicant.contactEmail ?: "No Email"
            linkText.text = applicant.socialLink ?: "No Link"
        }
    }
}