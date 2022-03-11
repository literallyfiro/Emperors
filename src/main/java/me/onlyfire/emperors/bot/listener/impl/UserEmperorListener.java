package me.onlyfire.emperors.bot.listener.impl;

import lombok.SneakyThrows;
import me.onlyfire.emperors.bot.*;
import me.onlyfire.emperors.bot.database.EmperorsDatabase;
import me.onlyfire.emperors.bot.listener.BotListener;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.concurrent.ExecutionException;

public record UserEmperorListener(EmperorsBot emperorsBot) implements BotListener {

    @Override
    public void execute(Update update, AbsSender sender) {
        Message message = update.getMessage();

        if (message == null) return;

        Chat chat = message.getChat();
        User user = message.getFrom();

        if (!message.hasText()) return;
        if (emperorsBot.getUserMode().containsKey(user)) return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));
        sendMessage.setReplyToMessageId(message.getMessageId());

        EmperorsDatabase database = emperorsBot.getDatabase();

        database.getEmperors(message.getChatId()).whenComplete((emperors, throwable) -> {
            if (throwable != null) {
                emperorsBot.generateErrorMessage(chat, new EmperorException("Errore nel database", throwable));
                return;
            }

            for (Emperor emperor : emperors) {
                if (emperor.getName().equals(message.getText().toLowerCase())) {
                    if (emperor.getTakenByName() == null || emperor.getTakenTime() == 0L) {
                        sendMessage.setText(takeEmperor(user, chat, message, emperor, emperors));
                    } else {
                        sendMessage.setText(emperor.getTakenById() == user.getId() ? Language.ALREADY_HAS_EMPEROR_SELF.toString() : String.format(Language.ALREADY_HAS_EMPEROR.toString(), emperor.getTakenByName(), emperor.getName()));
                    }

                    try {
                        sender.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        database.createGroupSettings(message.getChatId());
    }

    @SneakyThrows
    public String takeEmperor(User user, Chat chat, Message message, Emperor emperor, List<Emperor> groupEmperors) {
        Settings groupSettings = emperorsBot.getDatabase().getGroupSettings(chat.getId()).get();

        int same = 0;

        for (Emperor groupEmperor : groupEmperors) {
            if (groupEmperor.getTakenById() == user.getId()) {
                same++;
            }
        }

        if (same >= groupSettings.getMaxEmperorsPerUser()) {
            return String.format(Language.MAX_EMPERORS.toString(), same, groupSettings.getMaxEmperorsPerUser());
        }

        emperorsBot.getDatabase().takeEmperor(user, chat, message.getText());
        String joining = "Set %s (Familiar name: %s) new emperor - %s - of group %s (Familiar name: %s).";
        emperorsBot.getLogger().info(String.format(joining, user.getId(), user.getUserName() != null ? user.getFirstName() + " | @" + user.getUserName() : user.getFirstName(), message.getText(), message.getChatId(), chat.getTitle()));

        String photoUrl = "https://imgur.com/" + emperor.getPhotoId() + ".png";
        return String.format(Language.NEW_EMPEROR_OF_DAY.toString(), photoUrl, user.getFirstName(), emperor.getName());
    }
}
