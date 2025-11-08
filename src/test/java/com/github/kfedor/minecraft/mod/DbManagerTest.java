package com.github.kfedor.minecraft.mod;

import com.github.kfedor.minecraft.mod.db.DbManager;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Testcontainers
class DbManagerTest {

    @Container
    static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16.4");

    @BeforeAll
    static void startDb() {
        System.setProperty("db.url", container.getJdbcUrl());
        System.setProperty("db.username", container.getUsername());
        System.setProperty("db.password", container.getPassword());
        System.setProperty("db.poolSize", "2");

        DbManager.init();
    }

    @AfterAll
    static void stopDb() {
        DbManager.close();
    }

    @Test
    void insertMessageInsertsRow() throws Exception {
        UUID user = UUID.randomUUID();
        String text = "test-hello";

        DbManager.insertMessage(user, text);

        Thread.sleep(250);

        try (Connection connection =
                     DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword());
             PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT uuid, text FROM messages ORDER BY id DESC LIMIT 1");
             ResultSet resultSet = preparedStatement.executeQuery()) {

            assertTrue(resultSet.next(), "row must exist");
            assertEquals(user, resultSet.getObject("uuid"));
            assertEquals(text, resultSet.getString("text"));
        }
    }
}