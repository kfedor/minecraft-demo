# **MF Proto Demo (Fabric 1.21.8)**

Простой Fabric-мод: игрок вводит текст в GUI-окне, сообщение кодируется в Protobuf и отправляется на сервер, 
где сохраняется в PostgreSQL.

CREATE TABLE messages (
id   SERIAL PRIMARY KEY,
uuid UUID NOT NULL,
text VARCHAR(256) NOT NULL
);

## **⚙️ Технологии**

Java 21 • Gradle 8.14 • Fabric Loom 1.10.x
Fabric API • Mojang mappings
Protobuf 3 • PostgreSQL + HikariCP
JUnit 5 / Testcontainers
Логирование — SLF4J (LogUtils.getLogger)

## **💡 Основной функционал**

GUI: окно с полем ввода и кнопками Send / Cancel
Сеть: отправка Protobuf-payload с клиента на сервер (play C2S)
Сервер: парсинг, логирование, запись в БД
Конфигурация: application.properties (+ поддержка -Ddb.*)

## 📂 Основные классы

ModMain - entrypoint (инициализация, сеть, БД)
MessageScreen - интерфейс ввода текста
MessagePayload - Protobuf-payload
Network - регистрация приёмников (server C2S)
DbManager - HikariCP, schema, insert


## 🚀 Сборка

./gradlew clean build
./gradlew runClient

Файл application.properties:
db.url=jdbc:postgresql://localhost:5432/mc
db.username=postgres
db.password=admin
db.poolSize=4

## 🧪 Тесты
ProtobufTest — проверка сериализации.
DbManagerTest (Testcontainers)

Запуск:
./gradlew test

## 🧱 Логика без Hibernate

Для такого мода Spring Boot и JPA Repository избыточны — мод должен быть лёгким и быстро стартовать.
Здесь используется чистый JDBC через HikariCP, что:
•	снижает зависимостей и вес jar-файла;
•	исключает фреймворки, требующие Spring-контекста;
•	даёт полный контроль потоков (важно для сервера Minecraft).

### 🔄 Если бы использовался Hibernate + JPA
Я бы:
1.	Добавил hibernate-core и jakarta.persistence-api.
2.	Создал @Entity MessageEntity с полями id, uuid, text.
3.	Настроил SessionFactory через StandardServiceRegistryBuilder.
4.	Сделал лёгкий MessageRepository.save(UUID, String) с транзакцией.
5.	Инициализировал бы SessionFactory в ModMain, а вызовы save выполнял через executor.

## 📄 Лицензия

MIT