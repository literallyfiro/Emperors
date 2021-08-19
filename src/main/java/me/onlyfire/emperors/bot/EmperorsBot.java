package me.onlyfire.emperors.bot;

import lombok.Getter;
import me.onlyfire.emperors.bot.commands.*;
import me.onlyfire.emperors.bot.exceptions.EmperorException;
import me.onlyfire.emperors.bot.listener.ListenerManager;
import me.onlyfire.emperors.bot.listener.impl.AddEmperorListener;
import me.onlyfire.emperors.bot.listener.impl.AdminPanelListener;
import me.onlyfire.emperors.bot.listener.impl.RemoveEmperorListener;
import me.onlyfire.emperors.bot.listener.impl.UserEmperorListener;
import me.onlyfire.emperors.bot.mongo.EmperorsMongoDatabase;
import me.onlyfire.emperors.essential.BotVars;
import me.onlyfire.emperors.essential.Language;
import me.onlyfire.emperors.essential.StopAction;
import me.onlyfire.emperors.tasks.EmperorClearTask;
import me.onlyfire.emperors.user.EmperorUserMode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class EmperorsBot extends TelegramLongPollingCommandBot {

    private final BotVars botVars;
    private final ListenerManager listenerManager;
    private final EmperorsMongoDatabase mongoDatabase;

    private final Logger logger = LoggerFactory.getLogger("EmperorsBot");
    private final Map<User, EmperorUserMode> userMode = new HashMap<>();

    public EmperorsBot(BotVars botVars) {
        this.botVars = botVars;
        this.mongoDatabase = new EmperorsMongoDatabase();
        mongoDatabase.connect(botVars.uri());

        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(new EmperorClearTask(this), 0, 1, TimeUnit.SECONDS);

        register(new AddEmperorCommand(this));
        register(new RemoveEmperorCommand(this));
        register(new CancelCommand(this));
        register(new ListEmperorsCommand(this));
        register(new GlobalMessageCommand(this));
        register(new StartCommand());
        register(new AdminCommand());

        this.listenerManager = new ListenerManager();
        this.listenerManager.addListener(new AddEmperorListener(this));
        this.listenerManager.addListener(new RemoveEmperorListener(this));
        this.listenerManager.addListener(new UserEmperorListener(this));
        this.listenerManager.addListener(new AdminPanelListener(this));

        Runtime.getRuntime().addShutdownHook(new Thread(mongoDatabase::close));

//        Trello trelloApi = new TrelloImpl(botVars.trelloKey(), botVars.trelloAccessToken(), new AsyncTrelloHttpClient2());
//        this.trelloBoard = trelloApi.getBoard("Emperors");

        logger.info("Successfully started.");
    }

    @Override
    public String getBotUsername() {
        return botVars.username();
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        Message message = update.getMessage();
        if (message != null) {
            mongoDatabase.registerGroup(message.getChat());
            mongoDatabase.registerUser(message.getFrom());
        }
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

        logger.error("There was an error during Emperors_BOT run. Code ID: " + errorCode);
        String error = ExceptionUtils.getStackTrace(throwable).replace("at ", "-> ");
        logger.error(error);

        /* *********************************************************** */
        sendMessage.setChatId(String.valueOf(339169693));

        try {
            sendMessage.setText(String.format(Language.ERROR_EMPEROR_CREATION_LOG.toString(), chat.getId(), errorCode));
            execute(sendMessage);
            sendMessage.setText("<code>" + error + "</code>");
            execute(sendMessage);

            if (throwable.getCause() != null) {
                String cause = ExceptionUtils.getStackTrace(throwable.getCause()).replace("at ", "-> ");
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
