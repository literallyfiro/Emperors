/*
 * Copyright (c) 2021.
 * The Emperors project is controlled by the GNU General Public License v3.0.
 * You can find it in the LICENSE file on the GitHub repository.
 */

package me.onlyfire.emperors.bot;

import lombok.Getter;
import me.onlyfire.emperors.bot.commands.*;
import me.onlyfire.emperors.bot.exceptions.EmperorException;
import me.onlyfire.emperors.bot.listener.ListenerManager;
import me.onlyfire.emperors.bot.listener.impl.AddEmperorListener;
import me.onlyfire.emperors.bot.listener.impl.AdminPanelListener;
import me.onlyfire.emperors.bot.listener.impl.UserEmperorListener;
import me.onlyfire.emperors.database.EmperorsDatabase;
import me.onlyfire.emperors.essential.BotVars;
import me.onlyfire.emperors.essential.Language;
import me.onlyfire.emperors.essential.StopAction;
import me.onlyfire.emperors.tasks.EmperorClearTask;
import me.onlyfire.emperors.user.EmperorUserMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class EmperorsBot extends TelegramLongPollingCommandBot {

    private final BotVars botVars;

    private final ListenerManager listenerManager = new ListenerManager();
    private final EmperorsDatabase database = new EmperorsDatabase(this);
    private final Logger logger = LoggerFactory.getLogger("EmperorsBot");
    private final Map<User, EmperorUserMode> userMode = new HashMap<>();
    private final List<Long> chats = new ArrayList<>();

    public EmperorsBot(BotVars botVars) {
        this.botVars = botVars;

        database.connect(botVars.getUri());

        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(new EmperorClearTask(this), 0, 1, TimeUnit.SECONDS);

        register(new AddEmperorCommand(this));
        register(new RemoveEmperorCommand(this));
        register(new CancelCommand(this));
        register(new ListEmperorsCommand(this));
        register(new GlobalMessageCommand(this));
        register(new StartCommand());
        register(new AdminCommand());

        this.listenerManager.addListener(new AddEmperorListener(this));
        this.listenerManager.addListener(new UserEmperorListener(this));
        this.listenerManager.addListener(new AdminPanelListener(this));

        Runtime.getRuntime().addShutdownHook(new Thread(database::close));

        logger.info("Successfully started.");
    }

    @Override
    public String getBotUsername() {
        return botVars.getUsername();
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        Chat chat = update.getMessage().getChat();
        if (!chats.contains(chat.getId())) chats.add(chat.getId());
        listenerManager.executeUpdate(update, this);
    }

    @Override
    public String getBotToken() {
        return botVars.getToken();
    }

    public void removeUserMode(User user, Chat chat, EmperorException throwable) {
        userMode.get(user).stop();
        userMode.remove(user);

        if (throwable != null) {
            generateErrorMessage(chat, throwable);
        }
    }

    public void generateErrorMessage(Chat chat, EmperorException throwable) {
        var errorCode = generateErrorCode();

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));
        sendMessage.setText(String.format(Language.GENERAL_ERROR.toString(), errorCode, throwable.getMessage()));
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        /* *********************************************************** */
        String error = getStackTrace(throwable);
        File file = generateFile(error, errorCode);

        logger.error("There was an error during Emperors_BOT run. Code ID: " + errorCode);
        logger.error("Error file has been saved to " + file.getPath());

        SendDocument sendDocument = new SendDocument();
        sendDocument.setDocument(new InputFile(file));
        sendDocument.setChatId(String.valueOf(339169693));
        sendDocument.setCaption(String.format(Language.ERROR_EMPEROR_CREATION_LOG.toString(), chat.getId()));
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        /* *********************************************************** */
    }

    private File generateFile(String error, String errorCode) {
        File file = new File("error_" + errorCode + ".txt");
        FileOutputStream fos = null;
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            fos = new FileOutputStream(file, true);
            byte[] b= error.getBytes();
            fos.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private String generateErrorCode() {
        var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder((100000 + rnd.nextInt(900000)) + "-");
        for (int i = 0; i < 5; i++)
            sb.append(chars[rnd.nextInt(chars.length)]);

        return "#" + sb;
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString().replace("at ", "-> ");
    }

    public void handleStopAction(User user, StopAction action) {
        logger.info("Received a stop request from " + user.getFirstName() + " [" + user.getId() + "]");
        logger.info("Stopping the bot in 3 seconds...");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Closing threads and stopping the JVM, bye bye");
        System.exit(action == StopAction.STOP ? 0 : 100);
    }
}
