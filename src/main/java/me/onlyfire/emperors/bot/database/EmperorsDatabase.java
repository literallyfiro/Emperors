package me.onlyfire.emperors.bot.database;

import com.glyart.mystral.database.AsyncDatabase;
import com.glyart.mystral.database.Credentials;
import com.glyart.mystral.database.Mystral;
import com.ibatis.common.jdbc.ScriptRunner;
import lombok.RequiredArgsConstructor;
import me.onlyfire.emperors.bot.Emperor;
import me.onlyfire.emperors.bot.EmperorException;
import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.utils.SQLTypeMapping;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static me.onlyfire.emperors.bot.database.Queries.*;

@RequiredArgsConstructor
public class EmperorsDatabase {

    private final EmperorsBot emperorsBot;
    private AsyncDatabase asyncDb;

    public void connect(String uri) {
        String[] split = uri.split(";");
        Credentials credentials = Credentials.builder()
                .host(split[0])
                .user(split[1])
                .password(split[2])
                .schema(split[3])
                .pool(split[4])
                .port(Integer.parseInt(split[5]))
                .build();

        Executor executor = (command) -> new Thread(command).start();
        this.asyncDb = Mystral.newAsyncDatabase(credentials, executor);
        this.runScripts();
    }

    private void runScripts() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        InputStream in = loader.getResourceAsStream("tables.sql");
        assert in != null : "Tables script does not exist in the classpath";

        Connection connection = getConnection();
        assert connection != null : "Connection object is null";

        ScriptRunner runner = new ScriptRunner(connection, false, true);
        runner.setLogWriter(null);
        Reader reader = new InputStreamReader(in);
        try {
            runner.runScript(reader);
            connection.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

    }

    public CompletableFuture<Integer> createEmperor(Chat chat, String newEmperorName, String photoId) {
        return asyncDb.update(
                INSERT_EMPEROR,
                new Object[]{chat.getId(), newEmperorName, photoId},
                false,
                Types.BIGINT, Types.VARCHAR, Types.VARCHAR
        );
    }

    public void takeEmperor(User user, Chat chat, String emperorName) {
        ZoneId zoneId = ZoneId.of("Europe/Rome");
        ZonedDateTime startDateTime = ZonedDateTime.ofInstant(Instant.now(), zoneId).toLocalDate().atStartOfDay(zoneId);
        ZonedDateTime tomorrowDateTime = startDateTime.plusDays(1);

        long takenTime = tomorrowDateTime.toEpochSecond();

        asyncDb.update(
                UPDATE_EMPEROR,
                new Object[]{user.getId(), user.getFirstName(), takenTime, chat.getId(), emperorName},
                true,
                Types.BIGINT, Types.VARCHAR, Types.BIGINT, Types.BIGINT, Types.VARCHAR
        ).whenComplete((integer, exception) -> {
            if (exception != null) {
                emperorsBot.removeUserMode(user, chat, new EmperorException("There was an exception when taking an emperor", exception));
            }
        });
    }

    public void deleteEmperor(String name, Chat chat) {
        asyncDb.update(
                DELETE_EMPEROR,
                new Object[]{name, chat.getId()},
                true,
                Types.VARCHAR, Types.BIGINT
        ).whenComplete((integer, exception) -> {
            if (exception != null) {
                emperorsBot.generateErrorMessage(chat, new EmperorException("There was an exception when taking an emperor", exception));
            }
        });
    }

    public CompletableFuture<Emperor> getEmperor(long groupId, String name) {
        return asyncDb.queryForObject(
                SELECT_EMPEROR,
                new Object[]{groupId, name},
                (resultSet, rowNumber) -> generateEmperorObject(resultSet),
                Types.BIGINT, Types.VARCHAR
        );
    }

    public CompletableFuture<Map<String, Object>> getGroupSettings(long groupId) {
        return asyncDb.queryForObject(FETCH_SETTINGS, new Long[]{groupId}, (resultSet, rowNumber) -> {
            Map<String, Object> map = new HashMap<>();

            for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                String columnName = resultSet.getMetaData().getColumnName(i + 1);

                Class<?> sqlType = SQLTypeMapping.toClass(resultSet.getMetaData().getColumnType(i + 1));

                if (!columnName.equals("groupId")) {
                    map.put(columnName, resultSet.getObject(columnName, sqlType));
                }
            }

            return map;
        }, Types.BIGINT);
    }

    public CompletableFuture<Integer> updateGroupSettings(long groupId, String key, int value) {
        return asyncDb.update(
                String.format(UPDATE_SETTINGS, key),
                new Object[]{value, groupId},
                false,
                Types.INTEGER, Types.BIGINT
        );
    }

    public void createGroupSettings(long groupId) {
        asyncDb.update(CREATE_SETTINGS, new Long[]{groupId, groupId}, false, Types.BIGINT, Types.BIGINT).whenComplete((integer, exception) -> {
            if (exception != null) {
                emperorsBot.getLogger().warn("Could not create group settings - " + groupId);
                exception.printStackTrace();
            }
        });
    }

    public CompletableFuture<List<Emperor>> getEmperors(long groupId) {
        return asyncDb.queryForList(GET_EMPERORS_GROUP_ID, new Long[]{groupId}, (resultSet, rowNumber) -> generateEmperorObject(resultSet), Types.BIGINT);
    }

    public CompletableFuture<List<Emperor>> getEmperors() {
        return asyncDb.queryForList(FETCH_EMPERORS, null, (resultSet, rowNumber) -> generateEmperorObject(resultSet));
    }

    public void emitEmperor(Emperor emperor) {
        asyncDb.update(
                UPDATE_EMPEROR,
                new Object[]{0L, null, 0L, emperor.groupId(), emperor.name()},
                true,
                Types.BIGINT, Types.NULL, Types.BIGINT, Types.BIGINT, Types.VARCHAR
        ).whenComplete((integer, exception) -> {
            if (exception != null) {
                emperorsBot.getLogger().warn("Could not emit emperor" + emperor);
                exception.printStackTrace();
            }
        });
    }

    private Emperor generateEmperorObject(ResultSet resultSet) throws SQLException {
        long groupId = resultSet.getLong("groupId");
        String name = resultSet.getString("name");
        String photoId = resultSet.getString("photoId");
        long takenById = resultSet.getLong("takenById");
        String takenByName = resultSet.getString("takenByName");
        long takenTime = resultSet.getLong("takenTime");

        return new Emperor(groupId, name, photoId, takenById, takenByName, takenTime);
    }

    private Connection getConnection() {
        Optional<DataSource> optional = asyncDb.getDataSource();
        if (optional.isPresent()) {
            DataSource source = optional.get();
            try {
                return source.getConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void close() {
        Optional<DataSource> dataSourceOptional = asyncDb.getDataSource();
        try {
            if (dataSourceOptional.isPresent()) {
                Connection connection = dataSourceOptional.get().getConnection();
                if (!connection.isClosed()) {
                    connection.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
