package me.onlyfire.emperors.user.impl;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.exceptions.EmperorException;
import me.onlyfire.emperors.bot.mongo.EmperorsMongoDatabase;
import me.onlyfire.emperors.bot.mongo.models.MongoEmperor;
import me.onlyfire.emperors.bot.mongo.models.MongoGroup;
import me.onlyfire.emperors.essential.Language;
import me.onlyfire.emperors.user.EmperorUserMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class EmperorUserRemoval extends EmperorUserMode {

    private final EmperorsBot emperorsBot;
    private final AbsSender sender;
    private final User user;
    private final Chat chat;
    private final Instant instThen;

    public EmperorUserRemoval(EmperorsBot emperorsBot, AbsSender sender, User user, Chat chat, Message message) {
        super(emperorsBot, sender, user, chat, message);
        this.emperorsBot = emperorsBot;
        this.sender = sender;
        this.user = user;
        this.chat = chat;
        this.instThen = Instant.now();
    }

    @Override
    public void runCheck() {
        Instant now = Instant.now();
        Duration duration = Duration.between(instThen, now);
        long seconds = duration.getSeconds();
        long limit = TimeUnit.MINUTES.toSeconds(2);

        if (seconds > limit) {
            emperorsBot.getUserMode().remove(user);
            SendMessage sendMessage = new SendMessage();
            sendMessage.enableHtml(true);
            sendMessage.setChatId(String.valueOf(chat.getId()));
            sendMessage.setText("<a href=\"tg://user?id=" + user.getId() + "\">" + user.getFirstName() + "</a> " +
                    "Sei stato rimosso dalla modalità rimozione per non aver risposto al messaggio in 2 minuti");
            try {
                sender.executeAsync(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            stop();
        }
    }

    public void completed(Message updatedMessage, String emperorName) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));
        sendMessage.setText(String.format(Language.REMOVED_EMPEROR_SUCCESSFULLY.toString(), emperorName));

        EmperorsMongoDatabase database = emperorsBot.getMongoDatabase();
        MongoGroup mongoGroup = database.getMongoGroup(chat);
        if (mongoGroup == null)
            throw new EmperorException("Could not fetch group id " + chat.getId());
        MongoEmperor mongoEmperor = database.getEmperorByName(chat, emperorName);
        if (mongoEmperor == null)
            return;

        long processingTime = database.deleteEmperor(chat, mongoEmperor);
        emperorsBot.removeUserMode(user, chat, null);
        String joining = "Removed emperor %s on group %s (Familiar name: %s). Took %sms for completion.";
        emperorsBot.getLogger().info(String.format(joining, emperorName, chat.getId(), chat.getTitle(), processingTime));

        try {
            sender.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public Chat getChat() {
        return chat;
    }
}
