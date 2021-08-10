package me.onlyfire.emperors.essential;

import com.google.common.collect.ImmutableMap;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.onlyfire.emperors.model.Emperor;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Database {

    private static final HikariDataSource hikari;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final Map<String, String> PROPS = ImmutableMap.<String, String>builder()
            .put("useUnicode", "true")
            .put("characterEncoding", "utf8")

            // https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
            .put("cachePrepStmts", "true")
            .put("prepStmtCacheSize", "250")
            .put("prepStmtCacheSqlLimit", "2048")
            .put("useServerPrepStmts", "true")
            .put("useLocalSessionState", "true")
            .put("rewriteBatchedStatements", "true")
            .put("cacheResultSetMetadata", "true")
            .put("cacheServerConfiguration", "true")
            .put("elideSetAutoCommits", "true")
            .put("maintainTimeStats", "false")
            .put("alwaysSendSetIsolation", "false")
            .put("cacheCallableStmts", "true")

            // https://github.com/brettwooldridge/HikariCP/wiki/Rapid-Recovery
            .put("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)))
            .build();

    private static final int MAXIMUM_POOL_SIZE = (Runtime.getRuntime().availableProcessors() * 2) + 1;
    private static final int MINIMUM_IDLE = Math.min(MAXIMUM_POOL_SIZE, 10);

    private static final long MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30);
    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
    private static final long LEAK_DETECTION_THRESHOLD = TimeUnit.SECONDS.toMillis(60);

    static {
        HikariConfig factory = new HikariConfig();

        factory.setDriverClassName("com.mysql.cj.jdbc.Driver");
        factory.setJdbcUrl(String.format("jdbc:%s://%s:%s/%s", "mysql", "127.0.0.1", "3306", "emperors_db"));
        factory.setUsername("root");
        factory.setPassword("***REMOVED***");
        factory.setPoolName("emperors-pool");

        PROPS.forEach(factory::addDataSourceProperty);
        factory.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        factory.setMinimumIdle(MINIMUM_IDLE);
        factory.setMaxLifetime(MAX_LIFETIME);
        factory.setConnectionTimeout(CONNECTION_TIMEOUT);
        factory.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);
        factory.setConnectionTestQuery("SELECT 1");

        hikari = new HikariDataSource(factory);

        @Language("MySQL")
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS emperors (groupId VARCHAR(50) NOT NULL, name VARCHAR(20) NOT NULL, photoId VARCHAR(20), takenBy VARCHAR(32) DEFAULT NULL, takenByName VARCHAR(32) DEFAULT NULL, takenTime LONG);";
        try (Connection conn = hikari.getConnection()) {
            conn.createStatement().executeUpdate(CREATE_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static CompletableFuture<Integer> executeUpdate(@Language("MySQL") String sql, Object[] params) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        executor.execute(() -> {
            try (Connection conn = hikari.getConnection()) {
                PreparedStatement st = conn.prepareStatement(sql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        st.setObject(i + 1, params[i]);
                    }
                }
                future.complete(st.executeUpdate());
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public static CompletableFuture<ResultSet> executeQuery(@Language("MySQL") String sql) {
        return executeQuery(sql, null);
    }

    public static CompletableFuture<ResultSet> executeQuery(@Language("MySQL") String sql, Object[] params) {
        CompletableFuture<ResultSet> future = new CompletableFuture<>();
        executor.execute(() -> {
            try (Connection conn = hikari.getConnection()) {
                PreparedStatement st = conn.prepareStatement(sql);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        st.setObject(i + 1, params[i]);
                    }
                }
                future.complete(st.executeQuery());
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public static Collection<Emperor> fetchEmperors() throws SQLException {
        List<Emperor> emperors = new ArrayList<>();
        CompletableFuture<ResultSet> future = executeQuery("SELECT * FROM emperors;");
        future.whenComplete(((resultSet, throwable) -> {
            try {
                while (resultSet.next()) {
                    Emperor emperor = new Emperor(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3));
                    emperor.setTakenById(resultSet.getString(4));
                    emperor.setTakenByName(resultSet.getString(5));
                    emperor.setTakenTime(resultSet.getLong(6));
                    emperors.add(emperor);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
        return emperors;
    }

    public static void close() {
        hikari.close();
    }

}
