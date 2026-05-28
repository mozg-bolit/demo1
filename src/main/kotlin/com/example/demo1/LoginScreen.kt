package com.example.demo1

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import java.util.Collections

class LoginScreen(private val mainApp: MainApp) {
    // Состояние капчи теперь живет прямо здесь
    private val correctOrder = listOf(1, 2, 3, 4)
    private val currentOrder = correctOrder.shuffled().toMutableList()
    private var selectedIdx = -1

    fun getView(): VBox {
        val root = VBox(10.0).apply { padding = Insets(20.0); alignment = Pos.CENTER }
        val errorLabel = Label().apply { textFill = Color.RED }

        val loginField = TextField().apply { promptText = "Логин"; maxWidth = 200.0 }
        val passField = PasswordField().apply { promptText = "Пароль"; maxWidth = 200.0 }

        // Элементы капчи
        val sampleImage = ImageView(Image(javaClass.getResourceAsStream("/f0.png"))).apply { fitWidth = 80.0; isPreserveRatio = true }
        val grid = GridPane().apply { hgap = 5.0; vgap = 5.0; alignment = Pos.CENTER }

        // Функция отрисовки (и перерисовки) сетки пазла
        fun drawCaptcha() {
            grid.children.clear()
            for (i in 0..3) {
                // i % 2 вычисляет колонку (0 или 1), i / 2 вычисляет строку (0 или 1) — идеально для 2x2
                val img = ImageView(Image(javaClass.getResourceAsStream("/f${currentOrder[i]}.png"))).apply { fitWidth = 70.0; fitHeight = 70.0 }
                val btn = Button("", img).apply {
                    style = if (selectedIdx == i) "-fx-border-color: #2196F3; -fx-border-width: 3;" else ""
                    setOnAction {
                        if (selectedIdx == -1) selectedIdx = i // Выделяем первую картинку
                        else {
                            Collections.swap(currentOrder, selectedIdx, i) // Меняем местами со второй
                            selectedIdx = -1
                        }
                        drawCaptcha() // Обновляем UI после клика
                    }
                }
                grid.add(btn, i % 2, i / 2)
            }
        }
        drawCaptcha() // Рисуем первый раз

        val loginBtn = Button("Войти").apply {
            prefWidth = 200.0
            setOnAction {
                val login = loginField.text.trim()
                val pass = passField.text.trim()
                val user = DatabaseManager.findUser(login)

                // Компактный блок проверок через when
                when {
                    login.isBlank() || pass.isBlank() -> errorLabel.text = "Заполните поля!"
                    user?.blocked == true -> errorLabel.text = "Аккаунт заблокирован!"
                    DatabaseManager.authenticate(login, pass) == null -> {
                        errorLabel.text = "Неверный логин/пароль!"
                        DatabaseManager.handleFailedAttempt(login)
                    }
                    currentOrder != correctOrder -> {
                        errorLabel.text = "Соберите пазл правильно!"
                        DatabaseManager.handleFailedAttempt(login)
                    }
                    else -> { // Все проверки пройдены
                        DatabaseManager.resetFailedAttempts(login)
                        if (user!!.role == "ADMIN") mainApp.showAdminScreen() else mainApp.showUserScreen(user)
                    }
                }
            }
        }

        root.children.addAll(Label("Вход"), loginField, passField, Label("Образец:"), sampleImage, grid, loginBtn, errorLabel)
        return root
    }
}