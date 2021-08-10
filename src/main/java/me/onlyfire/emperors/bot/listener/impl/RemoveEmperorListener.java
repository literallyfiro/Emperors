package me.onlyfire.emperors.bot.listener.impl;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.exceptions.EmperorException;
import me.onlyfire.emperors.bot.listener.BotListener;
import me.onlyfire.emperors.bot.mongo.EmperorsMongoDatabase;
import me.onlyfire.emperors.bot.mongo.models.MongoEmperor;
import me.onlyfire.emperors.bot.mongo.models.MongoGroup;
import me.onlyfire.emperors.essential.Language;
import me.onlyfire.emperors.user.impl.EmperorUserRemoval;
import me.onlyfire.emperors.utils.MemberUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public record RemoveEmperorListener(EmperorsBot emperorsBot) implements BotListener {

    @Override
    public void execute(Update update, AbsSender sender) {
        Message message = update.getMessage();

        if (message == null)
            return;

        Chat chat = message.getChat();
        String groupId = String.valueOf(message.getChatId());
        User user = message.getFrom();
        if (!MemberUtils.isAdministrator(sender, user, chat))
            return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));
        sendMessage.setReplyToMessageId(message.getMessageId());

        if (!(emperorsBot.getUserMode().get(user) instanceof EmperorUserRemoval emperorUserRemoval))
            return;

        if (emperorUserRemoval.getChat().getId().equals(chat.getId())) {
            if (!message.hasText())
                return;

            String emperorName = message.getText().toLowerCase();

            EmperorsMongoDatabase database = emperorsBot().getMongoDatabase();
            MongoGroup mongoGroup = database.getMongoGroup(chat);
            if (mongoGroup == null)
                throw new EmperorException("Could not fetch group id " + groupId);

            MongoEmperor mongoEmperor = database.getEmperorByName(chat, emperorName);
            if (mongoEmperor == null) {
                sendMessage.setText(Language.NOT_EXIST_EMPEROR.toString());
                try {
                    sender.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                emperorsBot.removeUserMode(user, chat, null);
                return;
            }

            emperorUserRemoval.completed(message, emperorName);
        }
    }
}
