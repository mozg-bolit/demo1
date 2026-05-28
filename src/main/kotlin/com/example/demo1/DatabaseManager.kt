package com.example.demo1

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

// Модель данных пользователя
data class User(
    val login: String,
    var password: String,
    val role: String,         // "ADMIN" или "USER"
    var blocked: Boolean = false,
    var failedAttempts: Int = 0
)

object DatabaseManager {
    private val client = HttpClient(CIO)

    // === ДАННЫЕ-ЗАТЫЧКИ (Имитация базы данных в оперативной памяти) ===
    private val users = mutableListOf(
        User("admin", "admin123", "ADMIN"),
        User("user1", "pass1", "USER"),
        User("user2", "pass2", "USER", blocked = true) // Сразу заблокированный для теста
    )

    // Возвращает список всех пользователей для админки
    fun getAllUsers(): List<User> {
        return users.toList()
    }

    // Поиск пользователя по логину
    fun findUser(login: String): User? {
        return users.find { it.login.equals(login, ignoreCase = true) }
    }

    // Проверка логина и пароля при авторизации
    fun authenticate(login: String, password: String): User? {
        val user = findUser(login)
        return if (user != null && user.password == password) user else null
    }

    // Добавление нового пользователя через панель администратора
    fun addUser(login: String, password: String, role: String): Boolean {
        if (login.isBlank() || password.isBlank() || findUser(login) != null) return false
        users.add(User(login, password, role))
        return true
    }

    // Обновление данных (пароля или статуса блокировки)
    fun updateUser(login: String, update: (User) -> Unit) {
        val user = findUser(login) ?: return
        update(user)
    }

    // Логика обработки неудачной попытки входа (блокировка на 3-й раз)
    fun handleFailedAttempt(login: String) {
        val user = findUser(login) ?: return
        user.failedAttempts += 1
        if (user.failedAttempts >= 3) {
            user.blocked = true
        }
    }

    // Сброс счетчика ошибок при успешном входе и прохождении капчи
    fun resetFailedAttempts(login: String) {
        findUser(login)?.let { it.failedAttempts = 0 }
    }

    // Удаление пользователя из списка в админке
    fun deleteUser(login: String) {
        users.removeIf { it.login.equals(login, ignoreCase = true) }
    }

    /**
     * МОДУЛЬ 6: Безопасный запрос к API симулятора.
     * Защищает от падения (ошибки 500), если сервер шлет текст вместо JSON,
     * и очищает строковые ответы от мусора (% и &).
     */
    private suspend fun safeApiRequest(endpoint: String): String {
        return try {
            val response: HttpResponse = client.get("${ApiConfig.baseUrl}/$endpoint")
            val rawBody = response.bodyAsText().trim()

            // Если пришел JSON-объект — парсим, иначе работаем как с чистым текстом
            val cleanText = if (rawBody.startsWith("{") && rawBody.endsWith("}")) {
                Json.decodeFromString<ApiResponse>(rawBody).value
            } else {
                rawBody
            }
            // Очистка от спецсимволов % и & по ТЗ
            cleanText.replace("%", "").replace("&", " ").trim()
        } catch (e: Exception) {
            "Ошибка сервера"
        }
    }

    // Методы интеграции с API
    suspend fun fetchFullName() = safeApiRequest("fullName")
    suspend fun fetchSnils() = safeApiRequest("snils")
    suspend fun fetchInn() = safeApiRequest("inn")
    suspend fun fetchEmail() = safeApiRequest("email")
    suspend fun fetchIdentityCard() = safeApiRequest("identityCard")
}