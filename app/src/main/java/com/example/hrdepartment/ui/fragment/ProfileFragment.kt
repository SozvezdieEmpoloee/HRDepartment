package com.example.hrdepartment.ui.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.hrdepartment.MainActivity
import com.example.hrdepartment.R
import com.example.hrdepartment.data.models.ChangePasswordRequest
import com.example.hrdepartment.data.models.UserDto
import com.example.hrdepartment.network.ApiClient
import kotlinx.coroutines.launch
import retrofit2.Response

class ProfileFragment : Fragment() {

    private lateinit var profilePhoto: ImageView
    private lateinit var btnChangePhoto: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView

    private lateinit var btnChangePassword: Button
    private lateinit var btnDeleteAccount: Button
    private lateinit var btnLogout: Button

    private var currentUser: UserDto? = null

    // Регистрируем колбэк для выбора изображения из галереи
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // Обновим ImageView
            Glide.with(requireContext())
                .load(it)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .centerCrop()
                .into(profilePhoto)

            // Загружаем фото на сервер
            updateUserPhoto(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profilePhoto = view.findViewById(R.id.profile_photo)
        btnChangePhoto = view.findViewById(R.id.btn_change_photo)
        tvName = view.findViewById(R.id.tv_profile_name)
        tvEmail = view.findViewById(R.id.tv_profile_email)
        tvPhone = view.findViewById(R.id.tv_profile_phone)

        btnChangePassword = view.findViewById(R.id.btn_change_password)
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account)
        btnLogout = view.findViewById(R.id.btn_logout)



        btnChangePhoto.setOnClickListener {
            // Запускаем выбор фото из галереи
            pickImageLauncher.launch("image/*")
        }

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }

        btnLogout.setOnClickListener {
            logout()
        }
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("current_user_id", -1)
        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Запрашиваем user
        lifecycleScope.launch {
            val apiService = ApiClient.getApiService(requireContext(), requireAuth = true)
            try {
                val user = apiService.getUserById(userId)
                // Обновить UI
                updateUI(user)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadUserProfile() {
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("current_user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(requireContext(), requireAuth = true)
                val user = apiService.getUserById(userId) // Возвращает UserDto
                currentUser = user

                // Отображаем в UI
                updateUI(user)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI(user: UserDto) {
        tvName.text = user.name
        tvEmail.text = user.email
        tvPhone.text = user.phone

        // Загрузка фото (если есть photo_url)
        if (!user.photo_url.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(user.photo_url)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .centerCrop()
                .into(profilePhoto)
        } else {
            profilePhoto.setImageResource(R.drawable.ic_person_placeholder)
        }
    }

    private fun logout() {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    /**
     * Переход на экран входа (MainActivity)
     */
    private fun redirectToLogin() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    /**
     * Окно для смены пароля
     */
    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
        val etOldPassword = dialogView.findViewById<EditText>(R.id.et_old_password)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.et_new_password)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.et_confirm_password)

        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val oldPass = etOldPassword.text.toString().trim()
                val newPass = etNewPassword.text.toString().trim()
                val confirmPass = etConfirmPassword.text.toString().trim()

                if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                    Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPass != confirmPass) {
                    Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                changePassword(oldPass, newPass)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Логика смены пароля.
     */
    private fun changePassword(oldPass: String, newPass: String) {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("current_user_id", -1)
        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(requireContext(), requireAuth = true)
                val request = ChangePasswordRequest(
                    old_password = oldPass,
                    new_password = newPass
                )

                val response: Response<Unit> = apiService.changePassword(userId, request)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(requireContext(), "Error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error changing password: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Диалог для удаления аккаунта
     */
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("current_user_id", -1)
        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(requireContext(), requireAuth = true)
                val response: Response<Unit> = apiService.deleteUser(userId)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
                    logout()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(requireContext(), "Error deleting account: $errorMessage", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error deleting account: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    /**
     * Обновление фото пользователя на сервере.
     */
    private fun updateUserPhoto(uri: Uri) {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("current_user_id", -1)
        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(requireContext(), requireAuth = true)
                val updatedDto = currentUser?.copy(
                    photo_url = uri.toString()
                )
                if (updatedDto != null) {
                    val response: Response<UserDto> = apiService.updateUser(userId, updatedDto)
                    if (response.isSuccessful) {
                        currentUser = response.body()
                        updateUI(currentUser!!)
                        Toast.makeText(requireContext(), "Photo updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(requireContext(), "Error updating photo: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error updating photo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
