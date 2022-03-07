package me.onlyfire.emperors.bot.emperor;

import com.glyart.mystral.database.AsyncDatabase;
import com.glyart.mystral.database.Credentials;
import com.glyart.mystral.database.Mystral;
import com.ibatis.common.jdbc.ScriptRunner;
import lombok.RequiredArgsConstructor;
import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.EmperorException;
import org.intellij.lang.annotations.Language;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
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
        Reader reader = new InputStreamReader(in);
        try {
            runner.runScript(reader);
            connection.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

    }

    public CompletableFuture<Integer> createEmperor(Chat chat, String newEmperorName, String photoId) {
        @Language("MySQL") String sql = "INSERT INTO emperors (groupId, name, photoId) VALUES(?,?,?)";
        return asyncDb.update(sql, new Object[]{chat.getId(), newEmperorName, photoId}, true,
                Types.BIGINT, Types.VARCHAR, Types.VARCHAR);
    }

    public void settingsInsert(Chat chat) {
        @Language("MySQL") String sql = "INSERT INTO settings (groupId) SELECT * FROM (SELECT ? AS groupId) AS tmp WHERE NOT EXISTS (SELECT groupId FROM settings WHERE groupId = ?) LIMIT 1";
        asyncDb.update(sql, new Long[]{chat.getId(), chat.getId()}, false, Types.BIGINT, Types.BIGINT);
    }

    public long takeEmperor(User user, Chat chat, String emperorName) {
        AtomicLong processingTime = new AtomicLong();
        long initial = System.currentTimeMillis();

        ZoneId zoneId = ZoneId.of("Europe/Rome");
        ZonedDateTime startDateTime = ZonedDateTime.ofInstant(Instant.now(), zoneId).toLocalDate().atStartOfDay(zoneId);
        ZonedDateTime tomorrowDateTime = startDateTime.plusDays(1);

        long takenTime = tomorrowDateTime.toEpochSecond();

        @Language("MySQL") String sql = "UPDATE emperors SET takenById=?, takenByName=?, takenTime=? WHERE groupId=? AND name=?";
        CompletableFuture<Integer> future = asyncDb.update(sql, new Object[]{user.getId(), user.getFirstName(), takenTime, chat.getId(), emperorName}, true,
                Types.BIGINT, Types.VARCHAR, Types.BIGINT, Types.BIGINT, Types.VARCHAR);

        future.whenComplete((integer, exception) -> {
            if (exception != null) {
                emperorsBot.removeUserMode(user, chat, new EmperorException("There was an exception when taking an emperor", exception));
            }
            processingTime.set(System.currentTimeMillis() - initial);
        });

        return processingTime.get();
    }

    public long deleteEmperor(String name, long groupId) {
        AtomicLong processingTime = new AtomicLong();
        long initial = System.currentTimeMillis();

        @Language("MySQL") String sql = "DELETE FROM emperors WHERE name=? AND groupId=?";
        CompletableFuture<Integer> future = asyncDb.update(sql, new Object[]{name, groupId}, true,
                Types.VARCHAR, Types.BIGINT);

        future.whenComplete((integer, exception) -> processingTime.set(System.currentTimeMillis() - initial));

        return processingTime.get();
    }

    public CompletableFuture<Emperor> getEmperor(Long groupId, String name) {
        @Language("MySQL") String sql = "SELECT * FROM emperors WHERE groupId=? AND name=?";
        return asyncDb.queryForObject(sql, new Object[]{groupId, name}, (((resultSet, i) -> new Emperor(resultSet.getLong(2), resultSet.getString(3),
                resultSet.getString(4), resultSet.getLong(5),
                resultSet.getString(6), resultSet.getLong(7)))), Types.BIGINT, Types.VARCHAR);
    }

    public CompletableFuture<List<Emperor>> getEmperors(Long groupId) {
        @Language("MySQL") String sql = "SELECT * FROM emperors WHERE groupId=?";
        return asyncDb.queryForList(sql, new Long[]{groupId}, (resultSet, rowNumber) -> new Emperor(resultSet.getLong(2), resultSet.getString(3),
                resultSet.getString(4), resultSet.getLong(5),
                resultSet.getString(6), resultSet.getLong(7)), Types.BIGINT);
    }

    public CompletableFuture<List<Emperor>> getEmperors() {
        @Language("MySQL") String sql = "SELECT * FROM emperors";
        return asyncDb.queryForList(sql, null, (resultSet, rowNumber) -> new Emperor(resultSet.getLong(2), resultSet.getString(3),
                resultSet.getString(4), resultSet.getLong(5),
                resultSet.getString(6), resultSet.getLong(7)));
    }

    public void emitEmperor(Emperor emperor) {
        @Language("MySQL") String sql = "UPDATE emperors SET takenById=?, takenByName=?, takenTime=? WHERE groupId=? AND name=?";
        CompletableFuture<Integer> future = asyncDb.update(sql, new Object[]{0L, null, 0L, emperor.getGroupId(), emperor.getName()}, true,
                Types.BIGINT, Types.NULL, Types.BIGINT, Types.BIGINT, Types.VARCHAR);

        future.whenComplete((integer, exception) -> {
            if (exception != null) {
                emperorsBot.getLogger().warn("Could not emit emperor" + emperor);
            }
        });
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