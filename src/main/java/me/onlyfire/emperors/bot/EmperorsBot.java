package me.onlyfire.emperors.bot;

import lombok.Getter;
import me.onlyfire.emperors.bot.commands.*;
import me.onlyfire.emperors.bot.listener.ListenerManager;
import me.onlyfire.emperors.bot.listener.impl.AddEmperorListener;
import me.onlyfire.emperors.bot.listener.impl.UserEmperorListener;
import me.onlyfire.emperors.bot.emperor.EmperorsDatabase;
import me.onlyfire.emperors.BotVars;
import me.onlyfire.emperors.Language;
import me.onlyfire.emperors.bot.emperor.EmperorClearTask;
import me.onlyfire.emperors.bot.emperor.user.EmperorUserMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

        this.listenerManager.addListener(new AddEmperorListener(this));
        this.listenerManager.addListener(new UserEmperorListener(this));

        Runtime.getRuntime().addShutdownHook(new Thread(database::close));

        logger.info("Successfully started.");
    }

    @Override
    public String getBotUsername() {
        return botVars.getUsername();
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.getMessage() != null) {
            Chat chat = update.getMessage().getChat();
            if (!chat.isUserChat()) {
                database.settingsInsert(chat);
                if (!chats.contains(chat.getId())) chats.add(chat.getId());
            }
        }
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
    }

    private String generateErrorCode() {
        var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder((100000 + rnd.nextInt(900000)) + "-");
        for (int i = 0; i < 5; i++)
            sb.append(chars[rnd.nextInt(chars.length)]);

        return "#" + sb;
    }

}
