package com.github.kfedor.minecraft.mod.db;

import com.mojang.logging.LogUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Database facade: creates a connection pool, ensures schema, and persists messages.
 *
 * <p>Configuration is read from {@code application.properties} and can be overridden
 * by JVM properties ({@code -Ddb.url}, {@code -Ddb.username}, {@code -Ddb.password}, {@code -Ddb.poolSize}).</p>
 *
 * <p>Threading model: write operations are executed off-thread via an executor.</p>
 */
public final class DbManager {

    private static HikariDataSource hikariDataSource;
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(2);
    private static final Logger LOG = LogUtils.getLogger();

    private DbManager() {
    }

    /**
     * Initializes the connection pool and ensures that
     * the {@code messages} table exists.
     */
    public static void init() {
        LOG.info("Initializing DB connection pool...");
        Properties p = loadProperties();
        hikariDataSource = buildDataSource(p);
        ensureSchema(hikariDataSource);
        LOG.info("DB ready (pool created, schema ensured)");
    }

    /**
     * Inserts a row into {@code messages} asynchronously.
     *
     * @param uuid player UUID (message author)
     * @param text message text (should be trimmed to 256 chars by caller)
     */
    public static void insertMessage(UUID uuid, String text) {
        EXECUTOR_SERVICE.submit(() -> {
            try (Connection connection = hikariDataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "INSERT INTO messages(uuid, text) VALUES (?, ?)")) {
                preparedStatement.setObject(1, uuid);
                preparedStatement.setString(2, text);
                preparedStatement.executeUpdate();
                LOG.debug("Inserted message for {}", uuid);
            } catch (SQLException exception) {
                LOG.error("DB error inserting message for {}: SQLState={}, ErrorCode={}",
                        uuid, exception.getSQLState(), exception.getErrorCode(), exception);
            } catch (Exception exception) {
                LOG.error("Unexpected error inserting message for {}", uuid, exception);
            }
        });
    }

    /** Shuts down the executor and closes the pool. Safe to call on server stop. */
    public static void close() {
        try {
            EXECUTOR_SERVICE.shutdown();
        } catch (Exception ignored) {
        }
        try {
            if (hikariDataSource != null) {
                hikariDataSource.close();
            }
        } catch (Exception ignored) {
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = DbManager.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                LOG.error("Configuration file 'application.properties' not found in resources!");
                throw new IllegalStateException("application.properties not found");
            }
            properties.load(inputStream);
            LOG.info("Loaded DB configuration from application.properties");
        } catch (IOException ioException) {
            LOG.error("Failed to load application.properties: {}", ioException.getMessage(), ioException);
            throw new RuntimeException("Failed to load application.properties", ioException);
        }
        properties.putAll(System.getProperties());
        LOG.debug("DB config: url={}, user={}, pool={}",
                properties.getProperty("db.url"),
                properties.getProperty("db.username"),
                properties.getProperty("db.poolSize", "default"));
        return properties;
    }

    private static HikariDataSource buildDataSource(Properties p) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(Objects.requireNonNull(p.getProperty("db.url"), "db.url"));
        hikariConfig.setUsername(Objects.requireNonNull(p.getProperty("db.username"), "db.username"));
        hikariConfig.setPassword(Objects.requireNonNull(p.getProperty("db.password"), "db.password"));
        hikariConfig.setMaximumPoolSize(Integer.parseInt(p.getProperty("db.poolSize", "4")));
        return new HikariDataSource(hikariConfig);
    }

    private static void ensureSchema(HikariDataSource dataSource) {
        String ddl = """
                CREATE TABLE IF NOT EXISTS messages (
                  id   SERIAL PRIMARY KEY,
                  uuid UUID NOT NULL,
                  text VARCHAR(256) NOT NULL
                )
                """;
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute(ddl);
            LOG.info("Verified or created 'messages' table");
        } catch (Exception exception) {
            LOG.error("Failed to ensure database schema: {}", exception.getMessage(), exception);
            throw new RuntimeException("DB schema init failed", exception);
        }
    }
}
