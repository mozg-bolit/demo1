package com.example.demo1

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserScreen(private val mainApp: MainApp, private val user: User) {
    fun getView(): VBox {
        val root = VBox(15.0).apply { padding = Insets(25.0) }
        val title = Label("API Тест. Юзер: ${user.login}").apply { style = "-fx-font-size: 20px; -fx-font-weight: bold;" }

        val labCheckBox = CheckBox("Локальный сервер (Lab)").apply {
            isSelected = ApiConfig.useLabAddress
            setOnAction { ApiConfig.useLabAddress = this.isSelected }
        }

        val itemsContainer = VBox(10.0).apply {
            children.addAll(
                createRow("ФИО", { DatabaseManager.fetchFullName() }, { if (it.any { c -> c.isDigit() }) "Не успешно" else "Успешно" }),
                createRow("СНИЛС", { DatabaseManager.fetchSnils() }, { if (Regex("^\\d{3}-\\d{3}-\\d{3}\\s\\d{2}$").matches(it)) "Успешно" else "Не успешно" }),
                createRow("ИНН", { DatabaseManager.fetchInn() }, { if ((it.length == 10 || it.length == 12) && it.all { c -> c.isDigit() }) "Успешно" else "Не успешно" }),
                createRow("Email", { DatabaseManager.fetchEmail() }, { if (it.contains("@") && it.contains(".")) "Успешно" else "Не успешно" }),
                createRow("Карта", { DatabaseManager.fetchIdentityCard() }, { if (it.replace(" ", "").all { c -> c.isDigit() }) "Успешно" else "Не успешно" })
            )
        }

        val scrollPane = ScrollPane(itemsContainer).apply { isFitToWidth = true }
        val logoutBtn = Button("Выйти").apply { setOnAction { mainApp.showLoginScreen() } }

        root.children.addAll(title, labCheckBox, scrollPane, logoutBtn)
        return root
    }

    private fun createRow(name: String, fetch: suspend () -> String, valid: (String) -> String): VBox {
        val box = VBox(5.0).apply { style = "-fx-border-color: gray; -fx-padding: 10;" }
        val valLbl = Label("Значение: -")
        val statLbl = Label("Статус: -")
        val btn = Button(name).apply {
            setOnAction {
                valLbl.text = "Гружу..."
                CoroutineScope(Dispatchers.IO).launch {
                    val res = fetch()
                    Platform.runLater {
                        valLbl.text = "Значение: $res"
                        if (res == "Ошибка сервера") {
                            statLbl.text = "Статус: Ошибка"
                        } else {
                            val st = valid(res)
                            statLbl.text = "Статус: $st"
                            statLbl.textFill = if (st == "Успешно") Color.GREEN else Color.RED
                        }
                    }
                }
            }
        }
        box.children.addAll(valLbl, statLbl, btn)
        return box
    }
}