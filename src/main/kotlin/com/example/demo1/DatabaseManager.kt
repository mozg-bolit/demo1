package com.example.demo1

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import java.sql.Connection
import java.sql.DriverManager

data class User(
    val login: String,
    var password: String,
    val role: String,
    var blocked: Boolean = false,
    var failedAttempts: Int = 0
)

object DatabaseManager {
    private val client = HttpClient(CIO)

    // === НАСТРОЙКИ OPEN SERVER PANEL ===
    // allowPublicKeyRetrieval=true нужен для корректной работы с OSPanel
    private const val DB_URL = "jdbc:mysql://127.0.0.1:3306/mydb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
    private const val DB_USER = "root"
    private const val DB_PASSWORD = ""
    // Или "root", если с пустым не пускает
    // ВНИМАНИЕ: В Open Server Panel пароль по умолчанию часто ПУСТОЙ ("") или "root".
    // Если не подключается - поменяйте "" на "root"


    private fun getConnection(): Connection {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)
    }

    // === CRUD ОПЕРАЦИИ С БАЗОЙ ДАННЫХ ===

    fun getAllUsers(): List<User> {
        val list = mutableListOf<User>()
        try {
            getConnection().use { conn ->
                conn.prepareStatement("SELECT * FROM users").use { stmt ->
                    val rs = stmt.executeQuery()
                    while (rs.next()) {
                        list.add(User(rs.getString("login"), rs.getString("password"), rs.getString("role"), rs.getBoolean("blocked"), rs.getInt("failed_attempts")))
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return list
    }

    fun findUser(login: String): User? {
        try {
            getConnection().use { conn ->
                conn.prepareStatement("SELECT * FROM users WHERE login = ?").use { stmt ->
                    stmt.setString(1, login)
                    val rs = stmt.executeQuery()
                    if (rs.next()) return User(rs.getString("login"), rs.getString("password"), rs.getString("role"), rs.getBoolean("blocked"), rs.getInt("failed_attempts"))
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return null
    }

    fun authenticate(login: String, password: String): User? {
        val user = findUser(login)
        return if (user != null && user.password == password) user else null
    }

    fun addUser(login: String, password: String, role: String): Boolean {
        // Проверка: логин и пароль не пустые, и такого пользователя еще нет (Требование Модуля 4)
        if (login.isBlank() || password.isBlank() || findUser(login) != null) return false
        return try {
            getConnection().use { conn ->
                conn.prepareStatement("INSERT INTO users (login, password, role, blocked, failed_attempts) VALUES (?, ?, ?, FALSE, 0)").use { stmt ->
                    stmt.setString(1, login)
                    stmt.setString(2, password)
                    stmt.setString(3, role)
                    stmt.executeUpdate() > 0
                }
            }
        } catch (e: Exception) { false }
    }

    fun updateUser(login: String, update: (User) -> Unit) {
        val user = findUser(login) ?: return
        update(user)
        try {
            getConnection().use { conn ->
                conn.prepareStatement("UPDATE users SET password = ?, blocked = ?, failed_attempts = ? WHERE login = ?").use { stmt ->
                    stmt.setString(1, user.password)
                    stmt.setBoolean(2, user.blocked)
                    stmt.setInt(3, user.failedAttempts)
                    stmt.setString(4, user.login)
                    stmt.executeUpdate()
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // Увеличиваем счетчик ошибок. Блокируем, если ошибок 3 или больше.
    fun handleFailedAttempt(login: String) {
        val user = findUser(login) ?: return
        val newAttempts = user.failedAttempts + 1
        try {
            getConnection().use { conn ->
                conn.prepareStatement("UPDATE users SET failed_attempts = ?, blocked = ? WHERE login = ?").use { stmt ->
                    stmt.setInt(1, newAttempts)
                    stmt.setBoolean(2, newAttempts >= 3 || user.blocked)
                    stmt.setString(3, login)
                    stmt.executeUpdate()
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // Сбрасываем счетчик при успешном входе
    fun resetFailedAttempts(login: String) {
        try {
            getConnection().use { conn ->
                conn.prepareStatement("UPDATE users SET failed_attempts = 0 WHERE login = ?").use { stmt ->
                    stmt.setString(1, login)
                    stmt.executeUpdate()
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun deleteUser(login: String) {
        try {
            getConnection().use { conn ->
                conn.prepareStatement("DELETE FROM users WHERE login = ?").use { stmt ->
                    stmt.setString(1, login)
                    stmt.executeUpdate()
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // === СЕТЕВЫЕ ЗАПРОСЫ (МОДУЛЬ 6) ===
    private suspend fun safeApiRequest(endpoint: String): String {
        return try {
            val raw = client.get("${ApiConfig.baseUrl}/$endpoint").bodyAsText().trim()
            val text = if (raw.startsWith("{") && raw.endsWith("}")) Json.decodeFromString<ApiResponse>(raw).value else raw
            text.replace("%", "").replace("&", " ").trim()
        } catch (e: Exception) { "Ошибка сервера" }
    }

    suspend fun fetchFullName() = safeApiRequest("fullName")
    suspend fun fetchSnils() = safeApiRequest("snils")
    suspend fun fetchInn() = safeApiRequest("inn")
    suspend fun fetchEmail() = safeApiRequest("email")
    suspend fun fetchIdentityCard() = safeApiRequest("identityCard")
}