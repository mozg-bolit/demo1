package com.example.demo1

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color

class AdminScreen(private val mainApp: MainApp) {
    fun getView(): VBox {
        val root = VBox(15.0).apply { padding = Insets(20.0) }
        val usersBox = VBox(5.0) // Контейнер для строк пользователей

        // Функция отрисовки списка (вызывается при каждом изменении)
        fun drawUsers() {
            usersBox.children.clear()
            DatabaseManager.getAllUsers().forEach { u ->
                val row = HBox(10.0).apply { alignment = Pos.CENTER_LEFT }
                val status = Label(if (u.blocked) "БЛОК" else "ОК").apply { textFill = if (u.blocked) Color.RED else Color.GREEN; prefWidth = 40.0 }
                val passF = TextField().apply { promptText = "Новый пароль"; prefWidth = 120.0 }

                row.children.addAll(
                    Label(u.login).apply { prefWidth = 60.0 }, status, passF,
                    Button("ОК").apply { setOnAction { if (passF.text.isNotBlank()) DatabaseManager.updateUser(u.login) { it.password = passF.text }; drawUsers() } },
                    Button("Блок").apply { setOnAction { DatabaseManager.updateUser(u.login) { it.blocked = !it.blocked; it.failedAttempts = 0 }; drawUsers() } },
                    Button("Удалить").apply { setOnAction { DatabaseManager.deleteUser(u.login); drawUsers() } }
                )
                usersBox.children.add(row)
            }
        }

        // Панель добавления
        val addBox = HBox(10.0).apply {
            val logF = TextField().apply { promptText = "Логин" }
            val pasF = TextField().apply { promptText = "Пароль" }
            children.addAll(logF, pasF, Button("Создать").apply {
                setOnAction { DatabaseManager.addUser(logF.text, pasF.text, "USER"); logF.clear(); pasF.clear(); drawUsers() }
            })
        }

        drawUsers() // Заполняем список при старте

        root.children.addAll(Label("Админка").apply { style = "-fx-font-size: 20px;" }, addBox, ScrollPane(usersBox), Button("Выйти").apply { setOnAction { mainApp.showLoginScreen() } })
        return root
    }
}