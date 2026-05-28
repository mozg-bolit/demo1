package com.example.demo1

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import java.util.Collections

class LoginScreen(private val mainApp: MainApp) {
    private val correctOrder = listOf(1, 2, 3, 4)
    private val currentOrder = correctOrder.shuffled().toMutableList()
    private var selectedIdx = -1

    fun getView(): VBox {
        val root = VBox(10.0).apply { padding = Insets(20.0); alignment = Pos.CENTER }

        // Лейбл для ошибок стал шире и научился переносить текст
        val errorLabel = Label().apply {
            textFill = Color.RED
            isWrapText = true
            maxWidth = 250.0
            alignment = Pos.CENTER
            textAlignment = TextAlignment.CENTER
        }

        val loginField = TextField().apply { promptText = "Логин"; maxWidth = 200.0 }
        val passField = PasswordField().apply { promptText = "Пароль"; maxWidth = 200.0 }

        val sampleImage = ImageView(Image(javaClass.getResourceAsStream("/f0.png"))).apply { fitWidth = 80.0; isPreserveRatio = true }
        val grid = GridPane().apply { hgap = 5.0; vgap = 5.0; alignment = Pos.CENTER }

        fun drawCaptcha() {
            grid.children.clear()
            for (i in 0..3) {
                val img = ImageView(Image(javaClass.getResourceAsStream("/f${currentOrder[i]}.png"))).apply { fitWidth = 70.0; fitHeight = 70.0 }
                val btn = Button("", img).apply {
                    style = if (selectedIdx == i) "-fx-border-color: #2196F3; -fx-border-width: 3;" else ""
                    setOnAction {
                        if (selectedIdx == -1) selectedIdx = i
                        else {
                            Collections.swap(currentOrder, selectedIdx, i)
                            selectedIdx = -1
                        }
                        drawCaptcha()
                    }
                }
                grid.add(btn, i % 2, i / 2)
            }
        }
        drawCaptcha()

        val loginBtn = Button("Войти").apply {
            prefWidth = 200.0
            setOnAction {
                val login = loginField.text.trim()
                val pass = passField.text.trim()
                val user = DatabaseManager.findUser(login)

                when {
                    login.isBlank() || pass.isBlank() -> errorLabel.text = "Поля обязательны для заполнения!"

                    // Строго по тексту задания
                    user?.blocked == true -> errorLabel.text = "Вы заблокированы. Обратитесь к администратору"

                    // Строго по тексту задания
                    DatabaseManager.authenticate(login, pass) == null -> {
                        errorLabel.text = "Вы ввели неверный логин или пароль. Пожалуйста проверьте ещё раз введенные данные"
                        if (user != null) DatabaseManager.handleFailedAttempt(login)
                    }

                    currentOrder != correctOrder -> {
                        errorLabel.text = "Соберите пазл правильно!"
                        if (user != null) DatabaseManager.handleFailedAttempt(login)
                    }

                    else -> { // Все проверки пройдены
                        DatabaseManager.resetFailedAttempts(login)

                        // Всплывающее окно об успехе (строго по ТЗ)
                        Alert(Alert.AlertType.INFORMATION).apply {
                            title = "Авторизация"
                            headerText = null
                            contentText = "Вы успешно авторизовались"
                        }.showAndWait()

                        // Роль теперь проверяем на русском
                        if (user!!.role == "Администратор") mainApp.showAdminScreen() else mainApp.showUserScreen(user)
                    }
                }
            }
        }

        root.children.addAll(Label("Вход"), loginField, passField, Label("Образец:"), sampleImage, grid, loginBtn, errorLabel)
        return root
    }
}