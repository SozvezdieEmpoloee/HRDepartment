// app/src/main/java/com/example/hrdepartment/MainActivity.kt

package com.example.hrdepartment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hrdepartment.data.models.RegisterRequest
import com.example.hrdepartment.network.ApiClient
import com.example.hrdepartment.network.LoginRequest
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    // Инициализация переменных
    private lateinit var emailLog: EditText // Переименовано с email_log
    private lateinit var passwordLog: EditText
    private lateinit var loginBtn: Button

    private lateinit var nameRegister: EditText
    private lateinit var emailRegister: EditText
    private lateinit var phoneRegister: EditText
    private lateinit var specialCodeRegister: EditText
    private lateinit var passwordRegister: EditText
    private lateinit var confirmPasswordRegister: EditText
    private lateinit var registerBtn: Button

    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var loginLayout: View
    private lateinit var registerLayout: View

    private lateinit var needHelp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Устанавливаем соответствующий layout
        setContentView(R.layout.activity_main)

        // Инициализируем все views
        initializeViews()

        // Настраиваем переключение между логином и регистрацией
        setupToggleGroup()

        // Настраиваем видимость пароля
        setupPasswordVisibility()

        // Настраиваем обработчики кнопок
        setupButtonListeners()

        // Настраиваем ссылку "Need Help?"
        setupLink()



    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupPasswordVisibility() {
        // Настройка для логина
        setupTogglePasswordVisibility(passwordLog)

        // Настройка для регистрации
        setupTogglePasswordVisibility(passwordRegister)
        setupTogglePasswordVisibility(confirmPasswordRegister)
    }

    private fun setupTogglePasswordVisibility(editText: EditText) {
        // Добавление слушателя для нажатий на drawableEnd
        editText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = editText.compoundDrawables[2]
                if (drawableRight != null) {
                    val drawableWidth = drawableRight.bounds.width()
                    if (event.rawX >= (editText.right - drawableWidth - editText.paddingEnd)) {
                        togglePasswordVisibility(editText)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun togglePasswordVisibility(editText: EditText) {
        if (editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            // Изменить иконку на "видимый"
            editText.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                getDrawable(R.drawable.ic_eye), // Измените на соответствующую иконку
                null
            )
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            // Изменить иконку на "невидимый"
            editText.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                getDrawable(R.drawable.ic_eye), // Измените на соответствующую иконку
                null
            )
        }
        editText.setSelection(editText.text.length)
    }

    private fun initializeViews() {
        // Логин
        emailLog = findViewById(R.id.email_log) // Убедитесь, что ID соответствует "username_log" или измените его
        passwordLog = findViewById(R.id.password_log)
        loginBtn = findViewById(R.id.login_log)

        // Регистрация
        nameRegister = findViewById(R.id.name)
        emailRegister = findViewById(R.id.email_register)
        phoneRegister = findViewById(R.id.number)
        setupPhoneFormatting()
        specialCodeRegister = findViewById(R.id.special_code)
        passwordRegister = findViewById(R.id.password_register)
        confirmPasswordRegister = findViewById(R.id.confirm_password)
        registerBtn = findViewById(R.id.register_button)

        // Переключение между логином и регистрацией
        toggleGroup = findViewById(R.id.toggleGroup)
        loginLayout = findViewById(R.id.login_act)
        registerLayout = findViewById(R.id.register_act)

        // "Need Help?" ссылка
        needHelp = findViewById(R.id.need_help)
    }

    private fun setupPhoneFormatting() {
        phoneRegister.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val unformatted = s.toString().replace("[^\\d]".toRegex(), "")
                val formatted = when {
                    unformatted.length > 10 -> {
                        "+${unformatted.substring(0, 1)} (${unformatted.substring(1, 4)}) " +
                                "${unformatted.substring(4, 7)}-${unformatted.substring(7, 9)}-${unformatted.substring(9, 11)}"
                    }
                    unformatted.length > 6 -> {
                        "+${unformatted.substring(0, 1)} (${unformatted.substring(1, 4)}) " +
                                "${unformatted.substring(4, 7)}-${unformatted.substring(7)}"
                    }
                    unformatted.length > 4 -> {
                        "+${unformatted.substring(0, 1)} (${unformatted.substring(1, 4)}) " +
                                unformatted.substring(4)
                    }
                    else -> {
                        "+$unformatted"
                    }
                }

                phoneRegister.setText(formatted)
                phoneRegister.setSelection(formatted.length)
                isEditing = false
            }
        })
    }

    private fun setupToggleGroup() {
        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_log_in -> {
                        loginLayout.visibility = View.VISIBLE
                        registerLayout.visibility = View.GONE
                    }
                    R.id.sign_in -> {
                        loginLayout.visibility = View.GONE
                        registerLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupButtonListeners() {
        // Кнопка логина
        loginBtn.setOnClickListener {
            performLogin()
        }

        // Кнопка регистрации
        registerBtn.setOnClickListener {
            performRegistration()
        }
    }

    private fun performLogin() {
        val emailInput = emailLog.text.toString().trim()
        val passwordInput = passwordLog.text.toString().trim()

        if (emailInput.isEmpty() || passwordInput.isEmpty()) {
            showError("Email and Password are required.")
            return
        }

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@MainActivity, requireAuth = false)
                val loginRequest = LoginRequest(email = emailInput, password = passwordInput)

                val response = apiService.obtainToken(loginRequest) // Response<TokenResponse>
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse != null) {
                        // Сохранение токенов
                        saveTokens(tokenResponse.access, tokenResponse.refresh)
                        // Сохранение user_id
                        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                        val userId = tokenResponse.user_id
                        prefs.edit().putInt("current_user_id", userId).apply()

                        navigateToHomeScreen()
                    } else {
                        showError("Invalid login response.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    showError("Login failed. Code: ${response.code()} \n$errorBody")
                }
            } catch (e: Exception) {
                showError("An error occurred: ${e.message}")
            }
        }
    }

// app/src/main/java/com/example/hrdepartment/MainActivity.kt

    private fun performRegistration() {
        val name = nameRegister.text.toString().trim()
        val emailInput = emailRegister.text.toString().trim()
        val phone = phoneRegister.text.toString().trim()
        val specialCode = specialCodeRegister.text.toString().trim()
        val passwordInput = passwordRegister.text.toString().trim()
        val confirmPassword = confirmPasswordRegister.text.toString().trim()

        // Проверка полей
        if (name.isEmpty() || emailInput.isEmpty() || phone.isEmpty() ||
            specialCode.isEmpty() || passwordInput.isEmpty() || confirmPassword.isEmpty()
        ) {
            showError("All fields are required.")
            return
        }
        if (specialCode != "admin") {
            showError("Invalid special code.")
            return
        }
        if (passwordInput != confirmPassword) {
            showError("Passwords do not match.")
            return
        }

        // Логирование special_code для отладки
        Log.d("Registration", "Special code: $specialCode")

        lifecycleScope.launch {
            try {
                val apiService = ApiClient.getApiService(this@MainActivity, requireAuth = false)
                val registerRequest = RegisterRequest(
                    name = name,
                    email = emailInput,
                    password = passwordInput,
                    phone = phone,
                    photo_url = null,
                    special_code = specialCode
                )

                // Отправка запроса на регистрацию
                val response = apiService.registerUser(registerRequest) // Response<RegisterResponse>
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val userId = body.user_id
                        // Сохранение user_id в SharedPreferences
                        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                        prefs.edit().putInt("current_user_id", userId).apply()

                        showSuccess("Registration successful!")
                        navigateToHomeScreen()
                    } else {
                        showError("Registration response is empty")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    showError("Registration error: ${response.code()} $errorBody")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            }
        }
    }



    private fun clearTokens() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .remove("access_token")
            .remove("refresh_token")
            .apply()
    }



    private fun saveTokens(accessToken: String, refreshToken: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    private fun navigateToHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupLink() {
        val text = "Need Help?"
        val spannableString = SpannableString(text)

        // Устанавливаем ClickableSpan для текста "Need Help?"
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Открываем ссылку в браузере
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/video-25557243_456268352"))
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true // Подчеркивание
                ds.color = ds.linkColor // Цвет ссылки
            }
        }

        spannableString.setSpan(clickableSpan, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        needHelp.text = spannableString
        needHelp.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun showError(message: String) {
        Log.e("MainActivity", message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
