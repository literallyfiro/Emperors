package me.onlyfire.emperors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.onlyfire.emperors.bot.EmperorsBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.*;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Application {

    public static void main(String[] args) {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (!OS.contains("nux")) {
            System.out.println("Run this only from linux, thank you");
            return;
        }

        Logger logger = LoggerFactory.getLogger("EmperorsBot");
        logger.info("Starting Emperors Bot [BETA] - By ImOnlyFire");
        logger.info("Huge thanks to all of the MTG24 Team for making this project possible!");

        Properties props = loadProperties();
        BotVars botVars = new BotVars(props.getProperty("token"), props.getProperty("username"), props.getProperty("uri"), props.getProperty("imgur"));

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("main-thread").build();
        ExecutorService executor = Executors.newSingleThreadExecutor(namedThreadFactory);

        File file = new File("cache");
        if (!file.exists() && file.mkdirs())
            logger.info("Created cache directory, situated in " + file.getAbsolutePath());

        executor.execute(() -> {
            try {
                TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
                telegramBotsApi.registerBot(new EmperorsBot(botVars));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        File propsFile = new File("config.properties");
        if (!propsFile.exists()) {
            try (InputStream is = Application.class.getClassLoader().getResourceAsStream("config.properties")) {
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
