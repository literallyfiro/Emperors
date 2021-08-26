package me.onlyfire.emperors.bot;

import me.onlyfire.emperors.bot.commands.*;
import me.onlyfire.emperors.bot.exceptions.EmperorException;
import me.onlyfire.emperors.bot.listener.ListenerManager;
import me.onlyfire.emperors.bot.listener.impl.AddEmperorListener;
import me.onlyfire.emperors.bot.listener.impl.RemoveEmperorListener;
import me.onlyfire.emperors.bot.listener.impl.UserEmperorListener;
import me.onlyfire.emperors.essential.BotVars;
import me.onlyfire.emperors.essential.Database;
import me.onlyfire.emperors.essential.Language;
import me.onlyfire.emperors.essential.StopAction;
import me.onlyfire.emperors.model.Emperor;
import me.onlyfire.emperors.tasks.EmperorClearTask;
import me.onlyfire.emperors.user.EmperorUserMode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EmperorsBot extends TelegramLongPollingCommandBot {

    public final List<Emperor> loadedEmperors;
    public final BotVars botVars;
    public final ListenerManager listenerManager;

    public final Logger LOGGER = LoggerFactory.getLogger("EmperorsBot");
    public final Map<User, EmperorUserMode> userMode = new HashMap<>();

    public EmperorsBot(Collection<Emperor> loadedEmperors, BotVars botVars) {
        this.loadedEmperors = loadedEmperors.parallelStream().collect(Collectors.toList());
        this.botVars = botVars;

        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(new EmperorClearTask(this), 0, 1, TimeUnit.SECONDS);

        register(new AddEmperorCommand(this));
        register(new RemoveEmperorCommand(this));
        register(new CancelCommand(this));
        register(new ListEmperorsCommand(this));
        register(new StartCommand());

        this.listenerManager = new ListenerManager();
        this.listenerManager.addListener(new AddEmperorListener(this));
        this.listenerManager.addListener(new RemoveEmperorListener(this));
        this.listenerManager.addListener(new UserEmperorListener(this));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Closing bot.");
            Database.close();
        }));

        LOGGER.info("Successfully started.");
    }

    @Override
    public String getBotUsername() {
        return botVars.username();
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        listenerManager.executeUpdate(update, this);
    }

    @Override
    public String getBotToken() {
        return botVars.token();
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

        LOGGER.error("There was an error during Emperors_BOT run. Code ID: " + errorCode);
        String error = ExceptionUtils.getStackTrace(throwable).replace("at ", "-> ");
        LOGGER.error(error);

        /* *********************************************************** */
        sendMessage.setChatId(String.valueOf(339169693));

        try {
            sendMessage.setText(String.format(Language.ERROR_EMPEROR_CREATION_LOG.toString(), chat.getId(), errorCode));
            execute(sendMessage);
            sendMessage.setText("<code>" + error + "</code>");
            execute(sendMessage);

            if (throwable.getCause() != null) {
                String cause = getStackTrace(throwable.getCause()).replace("at ", "-> ");
                sendMessage.setText("Cause: <code>" + cause + "</code>");
            }

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        /* *********************************************************** */
    }

    private String generateErrorCode() {
        var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder((100000 + rnd.nextInt(900000)) + "-");
        for (int i = 0; i < 5; i++)
            sb.append(chars[rnd.nextInt(chars.length)]);

        return "#" + sb;
    }

    public void handleStopAction(User user, StopAction action) {
        LOGGER.info("Received a stop request from " + user.getFirstName() + " [" + user.getId() + "]");
        LOGGER.info("Stopping the bot in 3 seconds...");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("Closing threads and stopping the JVM, bye bye");
        System.exit(action == StopAction.STOP ? 0 : 100);
    }

    private String getStackTrace(Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

}
