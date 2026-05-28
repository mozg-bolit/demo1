package com.example.demo1

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class MainApp : Application() {
    // Корневой слой, на котором мы будем сменять экраны
    private val rootPane = StackPane()

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Система Авторизации и API"
        primaryStage.scene = Scene(rootPane, 850.0, 650.0)
        showLoginScreen() // При старте показываем логин
        primaryStage.show()
    }

    fun showLoginScreen() {
        rootPane.children.clear()
        rootPane.children.add(LoginScreen(this).getView())
    }

    fun showAdminScreen() {
        rootPane.children.clear()
        rootPane.children.add(AdminScreen(this).getView())
    }

    fun showUserScreen(user: User) {
        rootPane.children.clear()
        rootPane.children.add(UserScreen(this, user).getView())
    }
}

fun main() {
    Application.launch(MainApp::class.java)
}