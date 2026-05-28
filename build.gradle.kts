plugins {
    java
    application
    // Плагин Kotlin
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    // Плагин JavaFX (управляет версиями графических библиотек)
    id("org.openjfx.javafxplugin") version "0.1.0"
    // КРИТИЧЕСКИ ВАЖНО: Плагин для компиляции аннотации @Serializable
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    // Указываем точную точку входа — класс MainApp
    mainClass.set("com.example.demo1.MainApp")
}

kotlin {
    jvmToolchain(17)
}

javafx {
    version = "21.0.6"
    // Оставляем controls и graphics (модуль fxml больше не нужен для программного UI)
    modules = listOf("javafx.controls", "javafx.graphics")
}

dependencies {
    // Ktor для сетевых запросов к симулятору (Модуль 6)
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")

    // Сериализация JSON (исправляет ошибку в ApiConfig.kt)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Корутины для работы с фоновыми потоками сети и БД
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.7.3")

    // Драйвер для интеграции с базой данных MySQL Workbench
    implementation("com.mysql:mysql-connector-j:8.3.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}