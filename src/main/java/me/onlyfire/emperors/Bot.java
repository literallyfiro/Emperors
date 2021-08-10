package me.onlyfire.emperors;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.essential.BotVars;
import me.onlyfire.emperors.essential.Database;
import me.onlyfire.emperors.model.Emperor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.*;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Bot {

    public Bot() {
        Logger logger = LoggerFactory.getLogger("EmperorsBot");
        logger.info("Starting Emperors Bot [BETA] - By ImOnlyFire");
        logger.info("Huge thanks to all of the MTG24 Team for making this project possible!");

        Properties props = loadProperties();

        BotVars botVars = new BotVars(props.getProperty("token"), props.getProperty("username"),
                props.getProperty("mongodb_uri"), props.getProperty("trelloKey"), props.getProperty("trelloAccessToken"), props.getProperty("imgur"));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Collection<Emperor> emperors = Database.fetchEmperors();

            File file = new File("cache");
            if (!file.exists() && file.mkdirs())
                logger.info("Created cache directory, situated in " + file.getAbsolutePath());

            executor.execute(() -> {
                try {
                    TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
                    telegramBotsApi.registerBot(new EmperorsBot(emperors, botVars));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        File propsFile = new File("config.properties");
        if (!propsFile.exists()) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                props.load(is);
                try (OutputStream output = new FileOutputStream("config.properties")) {
                    props.store(output, null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (InputStream is = new FileInputStream("config.properties")) {
                props.load(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return props;
    }

}
