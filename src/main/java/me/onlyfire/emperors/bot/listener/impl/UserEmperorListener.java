package me.onlyfire.emperors.bot.listener.impl;

import lombok.SneakyThrows;
import me.onlyfire.emperors.bot.Emperor;
import me.onlyfire.emperors.bot.EmperorException;
import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.Language;
import me.onlyfire.emperors.bot.database.EmperorsDatabase;
import me.onlyfire.emperors.bot.listener.BotListener;
import me.onlyfire.emperors.cooldown.Cooldown;
import me.onlyfire.emperors.cooldown.CooldownManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public record UserEmperorListener(EmperorsBot emperorsBot) implements BotListener {


    @Override
    public void execute(Update update, AbsSender sender) {
        Message message = update.getMessage();

        if (message == null) return;

        Chat chat = message.getChat();
        User user = message.getFrom();

        if (user.getIsBot()) return;

        if (!message.hasText()) return;
        if (emperorsBot.getUserMode().containsKey(user)) return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));
        sendMessage.setReplyToMessageId(message.getMessageId());

        EmperorsDatabase database = emperorsBot.getDatabase();

        try {
            List<Emperor> emperors = database.getEmperors(message.getChatId()).get();
            for (Emperor emperor : emperors) {
                if (emperor.name().equals(message.getText().toLowerCase())) {
                    if (emperor.takenByName() == null || emperor.takenTime() == 0L) {
                        sendMessage.setText(takeEmperor(user, chat, message, emperor, emperors));
                    } else {
                        if (emperor.takenById() == user.getId()) {
                            sendMessage.setText(Language.ALREADY_HAS_EMPEROR_SELF.toString());
                        } else {
                            sendMessage.setText(String.format(Language.ALREADY_HAS_EMPEROR.toString(), emperor.takenByName(), emperor.name()));
                        }
                    }

                    try {
                        sender.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            emperorsBot.generateErrorMessage(chat, new EmperorException("Errore nel database", e));
        }

        database.createGroupSettings(message.getChatId());
    }

    @SneakyThrows
    public String takeEmperor(User user, Chat chat, Message message, Emperor emperor, List<Emperor> groupEmperors) {
        Map<String, Object> groupSettings = emperorsBot.getDatabase().getGroupSettings(chat.getId()).get();
        int maxEmperorsPerUser = (int) groupSettings.getOrDefault("maxEmperorsPerUser", 2);
        int emperorCooldown = (int) groupSettings.getOrDefault("emperorCooldown", 10);

        if (CooldownManager.getInstance().isInCooldown(user.getId(), chat)) {
            Cooldown cooldown = CooldownManager.getInstance().getCooldown(user.getId(), chat);
            return String.format(Language.IN_COOLDOWN.toString(), cooldown.getTimeRemaining());
        }

        int same = 0;

        for (Emperor groupEmperor : groupEmperors) {
            if (groupEmperor.takenById() == user.getId()) {
                same++;
            }
        }

        if (same >= maxEmperorsPerUser) {
            return String.format(Language.MAX_EMPERORS.toString(), same, maxEmperorsPerUser);
        }

        emperorsBot.getDatabase().takeEmperor(user, chat, message.getText());
        String joining = "Set %s (Familiar name: %s) new emperor - %s - of group %s (Familiar name: %s).";
        emperorsBot.getLogger().info(String.format(joining, user.getId(), user.getUserName() != null ? user.getFirstName() + " | @" + user.getUserName() : user.getFirstName(), message.getText(), message.getChatId(), chat.getTitle()));

        CooldownManager.getInstance().createCooldown(user.getId(), chat, emperorCooldown, TimeUnit.SECONDS);

        String photoUrl = "https://imgur.com/" + emperor.photoId() + ".png";
        return String.format(Language.NEW_EMPEROR_OF_DAY.toString(), photoUrl, user.getFirstName(), emperor.name());
    }
}
