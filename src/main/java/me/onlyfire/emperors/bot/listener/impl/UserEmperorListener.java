package me.onlyfire.emperors.bot.listener.impl;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.exceptions.EmperorException;
import me.onlyfire.emperors.bot.listener.BotListener;
import me.onlyfire.emperors.bot.mongo.EmperorsMongoDatabase;
import me.onlyfire.emperors.bot.mongo.models.MongoEmperor;
import me.onlyfire.emperors.bot.mongo.models.MongoGroup;
import me.onlyfire.emperors.bot.mongo.models.MongoTakenEmperor;
import me.onlyfire.emperors.bot.mongo.models.MongoUser;
import me.onlyfire.emperors.essential.Language;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public record UserEmperorListener(EmperorsBot emperorsBot) implements BotListener {

    @Override
    public void execute(Update update, AbsSender sender) {
        Message message = update.getMessage();

        if (message == null)
            return;

        Chat chat = message.getChat();
        String groupId = String.valueOf(message.getChatId());
        User user = message.getFrom();

        if (emperorsBot.getUserMode().containsKey(user))
            return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));
        sendMessage.setReplyToMessageId(message.getMessageId());

        EmperorsMongoDatabase database = emperorsBot.getMongoDatabase();
        MongoGroup mongoGroup = database.getMongoGroup(chat);
        MongoEmperor mongoEmperor = database.getEmperorByName(chat, message.getText());

        if (mongoGroup == null)
            throw new EmperorException("Could not fetch group id " + groupId);
        if (mongoEmperor == null)
            return;

        var takenEmperor = database.getTakenEmperor(user, chat, mongoEmperor);
        if (takenEmperor == null)
            return;

        if (takenEmperor.getTakenById().equals(user.getId())) {
            sendMessage.setText(Language.ALREADY_HAS_EMPEROR_SELF.toString());
            try {
                sender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }

        for (MongoUser mongoUsers : database.getAllMongoUsers()) {
            MongoTakenEmperor takenEmperors = database.getTakenEmperor(mongoUsers, chat, mongoEmperor);
            if (takenEmperors == null)
                return;

            sendMessage.setText(String.format(Language.ALREADY_HAS_EMPEROR.toString(),
                    takenEmperors.getTakenByName(), takenEmperors.getName()));
            try {
                sender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        long processingTime = database.takeEmperor(user, chat, mongoEmperor);
        String joining = "Set %s (Familiar name: %s) new emperor - %s - of group %s (Familiar name: %s). Took %sms.";
        emperorsBot.getLogger().info(String.format(joining, user.getId(),
                user.getUserName() != null ? user.getFirstName() + " | @" + user.getUserName() : user.getFirstName(),
                mongoEmperor.getName(), groupId, chat.getTitle(), processingTime));

        String photoUrl = "https://imgur.com/" + mongoEmperor.getPhotoId() + ".png";
        sendMessage.setText(String.format(Language.NEW_EMPEROR_OF_DAY.toString(),
                photoUrl, user.getFirstName(), mongoEmperor.getName()));
        try {
            sender.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
