package com.example.hrdepartment.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hrdepartment.R
import com.example.hrdepartment.data.models.EmployeeDto
import com.example.hrdepartment.network.ApiClient
import kotlinx.coroutines.launch
import retrofit2.Response


class EmployeesFragment : Fragment() {

    private lateinit var employeeRecyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var employeesAdapter: EmployeesAdapter // Объявляем адаптер как член-класса

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_employees, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("EmployeesFragment", "onViewCreated called")

        employeeRecyclerView = view.findViewById(R.id.employee_list)
        addButton = view.findViewById(R.id.btn_add_employee)

        employeeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Изначально пустой список
        employeesAdapter = EmployeesAdapter(
            mutableListOf(),
            onDelete = ::onEmployeeDelete,
            onEdit = ::onEmployeeEdit,
            onToggleDetails = ::onToggleDetails
        )
        employeeRecyclerView.adapter = employeesAdapter

        addButton.setOnClickListener {
            showAddEmployeeDialog()
        }

        // Загружаем реальный список с сервера
        loadEmployees()
    }

    private fun loadEmployees() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(requireContext(), requireAuth = true)
                val employeesFromServer = apiService.getEmployees() // GET /api/employees/
                employeesAdapter.updateList(employeesFromServer.toMutableList())
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error loading employees: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showAddEmployeeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_employee, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Employee")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val firstName = dialogView.findViewById<EditText>(R.id.et_employee_name).text.toString().trim()
                val lastName = dialogView.findViewById<EditText>(R.id.et_employee_surname).text.toString().trim()
                val email = dialogView.findViewById<EditText>(R.id.et_employee_email).text.toString().trim()
                val phone = dialogView.findViewById<EditText>(R.id.et_employee_phone).text.toString().trim()
                val salaryText = dialogView.findViewById<EditText>(R.id.et_employee_salary).text.toString().trim()
                val gender = dialogView.findViewById<EditText>(R.id.et_employee_gender).text.toString().trim()
                val ageText = dialogView.findViewById<EditText>(R.id.et_employee_age).text.toString().trim()
                val nationality = dialogView.findViewById<EditText>(R.id.et_employee_nationality).text.toString().trim()
                val placeOfResidence = dialogView.findViewById<EditText>(R.id.et_employee_place_of_residence).text.toString().trim()
                val characteristics = dialogView.findViewById<EditText>(R.id.et_employee_characteristics).text.toString().trim()

                val salary = if (salaryText.isNotEmpty()) salaryText.toDoubleOrNull() else null
                val age = if (ageText.isNotEmpty()) ageText.toIntOrNull() else null

                if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                    val newEmployeeDto = EmployeeDto(
                        first_name = firstName,
                        last_name = lastName,
                        email = if (email.isNotEmpty()) email else null,
                        phone = if (phone.isNotEmpty()) phone else null,
                        photo_url = null, // Или добавьте возможность загрузки фото
                        salary = salary,
                        gender = if (gender.isNotEmpty()) gender else null,
                        age = age,
                        nationality = if (nationality.isNotEmpty()) nationality else null,
                        place_of_residence = if (placeOfResidence.isNotEmpty()) placeOfResidence else null,
                        characteristics = if (characteristics.isNotEmpty()) characteristics else null,
                        created_at = null,
                        updated_at = null
                    )
                    addEmployeeToServer(newEmployeeDto)
                } else {
                    Toast.makeText(requireContext(), "First name and Last name are required.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun addEmployeeToServer(dto: EmployeeDto) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(requireContext(), requireAuth = true)
                val createdEmployee = apiService.createEmployee(dto)
                // обновляем локальный список
                val currentList = employeesAdapter.getList()
                currentList.add(createdEmployee)
                employeesAdapter.updateList(currentList)
                Toast.makeText(requireContext(), "Employee added!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error adding employee: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onEmployeeDelete(position: Int) {
        val employee = employeesAdapter.getList()[position]
        val id = employee.id
        if (id == null) {
            // Этот сотрудник не сохранён на сервере
            employeesAdapter.removeAt(position)
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete ${employee.first_name} ${employee.last_name}?")
            .setPositiveButton("Yes") { _, _ ->
                deleteEmployeeOnServer(id, position)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteEmployeeOnServer(employeeId: Int, position: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(requireContext(), requireAuth = true)
                val response: Response<Unit> = apiService.deleteEmployee(employeeId) // Теперь возвращает Response<Unit>
                Log.d("DeleteEmployee", "Response Code: ${response.code()}")
                Log.d("DeleteEmployee", "Is Successful: ${response.isSuccessful}")
                if (response.isSuccessful) {
                    employeesAdapter.removeAt(position)
                    Toast.makeText(requireContext(), "Employee deleted!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error deleting: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error deleting employee: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onEmployeeEdit(position: Int) {
        val employee = employeesAdapter.getList()[position]
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_employee, null)

        val firstNameField = dialogView.findViewById<EditText>(R.id.et_employee_name)
        val lastNameField = dialogView.findViewById<EditText>(R.id.et_employee_surname)
        val emailField = dialogView.findViewById<EditText>(R.id.et_employee_email)
        val phoneField = dialogView.findViewById<EditText>(R.id.et_employee_phone)
        val salaryField = dialogView.findViewById<EditText>(R.id.et_employee_salary)
        val genderField = dialogView.findViewById<EditText>(R.id.et_employee_gender)
        val ageField = dialogView.findViewById<EditText>(R.id.et_employee_age)
        val nationalityField = dialogView.findViewById<EditText>(R.id.et_employee_nationality)
        val placeOfResidenceField = dialogView.findViewById<EditText>(R.id.et_employee_place_of_residence)
        val characteristicsField = dialogView.findViewById<EditText>(R.id.et_employee_characteristics)

        // Заполняем текущие значения
        firstNameField.setText(employee.first_name)
        lastNameField.setText(employee.last_name)
        emailField.setText(employee.email ?: "")
        phoneField.setText(employee.phone ?: "")
        salaryField.setText(employee.salary?.toString() ?: "")
        genderField.setText(employee.gender ?: "")
        ageField.setText(employee.age?.toString() ?: "")
        nationalityField.setText(employee.nationality ?: "")
        placeOfResidenceField.setText(employee.place_of_residence ?: "")
        characteristicsField.setText(employee.characteristics ?: "")

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Employee")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val firstName = firstNameField.text.toString().trim()
                val lastName = lastNameField.text.toString().trim()
                val email = emailField.text.toString().trim()
                val phone = phoneField.text.toString().trim()
                val salaryText = salaryField.text.toString().trim()
                val gender = genderField.text.toString().trim()
                val ageText = ageField.text.toString().trim()
                val nationality = nationalityField.text.toString().trim()
                val placeOfResidence = placeOfResidenceField.text.toString().trim()
                val characteristics = characteristicsField.text.toString().trim()

                val salary = if (salaryText.isNotEmpty()) salaryText.toDoubleOrNull() else null
                val age = if (ageText.isNotEmpty()) ageText.toIntOrNull() else null

                if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                    val updatedEmployeeDto = employee.copy(
                        first_name = firstName,
                        last_name = lastName,
                        email = if (email.isNotEmpty()) email else null,
                        phone = if (phone.isNotEmpty()) phone else null,
                        salary = salary,
                        gender = if (gender.isNotEmpty()) gender else null,
                        age = age,
                        nationality = if (nationality.isNotEmpty()) nationality else null,
                        place_of_residence = if (placeOfResidence.isNotEmpty()) placeOfResidence else null,
                        characteristics = if (characteristics.isNotEmpty()) characteristics else null,
                        updated_at = null // Обычно сервер обновляет это поле
                    )
                    updateEmployeeOnServer(updatedEmployeeDto, position)
                } else {
                    Toast.makeText(requireContext(), "First name and Last name are required.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun updateEmployeeOnServer(dto: EmployeeDto, position: Int) {
        val id = dto.id ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(requireContext(), requireAuth = true)
                val updatedEmployee: EmployeeDto = apiService.updateEmployee(id, dto) // Убедитесь, что метод возвращает EmployeeDto
                // обновляем локальный список
                val currentList = employeesAdapter.getList()
                currentList[position] = updatedEmployee
                employeesAdapter.updateList(currentList)
                Toast.makeText(requireContext(), "Employee updated!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error updating employee: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onToggleDetails(position: Int) {
        val viewHolder = employeeRecyclerView.findViewHolderForAdapterPosition(position) as? EmployeesAdapter.ViewHolder
        viewHolder?.toggleDetails()
    }
}


class EmployeesAdapter(
    private var employees: MutableList<EmployeeDto>,
    private val onDelete: (Int) -> Unit,
    private val onEdit: (Int) -> Unit,
    private val onToggleDetails: (Int) -> Unit
) : RecyclerView.Adapter<EmployeesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_employee, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = employees.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(employees[position])
        holder.btnEdit.setOnClickListener {
            onEdit(position)
        }
        holder.btnDelete.setOnClickListener {
            onDelete(position)
        }
        holder.itemView.setOnClickListener {
            onToggleDetails(position)
        }
    }

    fun updateList(newList: MutableList<EmployeeDto>) {
        employees = newList
        notifyDataSetChanged()
    }

    fun getList(): MutableList<EmployeeDto> = employees

    fun removeAt(position: Int) {
        employees.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photo: ImageView = itemView.findViewById(R.id.employee_photo)
        private val name: TextView = itemView.findViewById(R.id.employee_name)
        // Удалено поле department
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)
        private val detailLayout: LinearLayout = itemView.findViewById(R.id.employee_detail_layout)

        // Дополнительные поля для раскрытия деталей
        private val email: TextView = itemView.findViewById(R.id.tv_employee_email)
        private val phone: TextView = itemView.findViewById(R.id.tv_employee_phone)
        private val salary: TextView = itemView.findViewById(R.id.tv_employee_salary)
        private val gender: TextView = itemView.findViewById(R.id.tv_employee_gender)
        private val age: TextView = itemView.findViewById(R.id.tv_employee_age)
        private val nationality: TextView = itemView.findViewById(R.id.tv_employee_nationality)
        private val placeOfResidence: TextView = itemView.findViewById(R.id.tv_employee_place_of_residence)
        private val characteristics: TextView = itemView.findViewById(R.id.tv_employee_characteristics)

        fun bind(employee: EmployeeDto) {
            // Используем Glide для загрузки изображения по URL, если photo_url не null
            if (!employee.photo_url.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(employee.photo_url)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .into(photo)
            } else {
                photo.setImageResource(R.drawable.ic_person_placeholder)
            }

            name.text = "${employee.first_name} ${employee.last_name}"

            // Заполняем детали
            email.text = "Email: ${employee.email ?: "N/A"}"
            phone.text = "Phone: ${employee.phone ?: "N/A"}"
            salary.text = "Salary: ${employee.salary?.toString() ?: "N/A"}"
            gender.text = "Gender: ${employee.gender ?: "N/A"}"
            age.text = "Age: ${employee.age?.toString() ?: "N/A"}"
            nationality.text = "Nationality: ${employee.nationality ?: "N/A"}"
            placeOfResidence.text = "Place: ${employee.place_of_residence ?: "N/A"}"
            characteristics.text = "Characteristics: ${employee.characteristics ?: "N/A"}"

            // Изначально скрываем детальную часть
            detailLayout.visibility = View.GONE
        }

        fun toggleDetails() {
            if (detailLayout.visibility == View.GONE) {
                detailLayout.visibility = View.VISIBLE
            } else {
                detailLayout.visibility = View.GONE
            }
        }
    }
}




data class Employee(
    var name: String,
    var surname: String,
    var department: String,
    val photo: Int
)
