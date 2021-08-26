package me.onlyfire.emperors.bot.listener.impl;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.listener.BotListener;
import me.onlyfire.emperors.database.Emperor;
import me.onlyfire.emperors.database.EmperorsDatabase;
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
        User user = message.getFrom();

        if (!message.hasText())
            return;
        if (emperorsBot.getUserMode().containsKey(user))
            return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));
        sendMessage.setReplyToMessageId(message.getMessageId());

        EmperorsDatabase database = emperorsBot.getDatabase();
        database.getEmperor(message.getChatId(), message.getText().toLowerCase()).whenComplete((emperor, exception) -> {
            if (emperor == null) return;
            if (emperor.getTakenByName() == null || emperor.getTakenTime() == 0L) {
                sendMessage.setText(takeEmperor(user, chat, message, emperor));
            } else {
                sendMessage.setText(emperor.getTakenById() == user.getId() ? Language.ALREADY_HAS_EMPEROR_SELF.toString()
                        : String.format(Language.ALREADY_HAS_EMPEROR.toString(), emperor.getTakenByName(), emperor.getName()));
            }
            try {
                sender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });

    }

    public String takeEmperor(User user, Chat chat, Message message, Emperor emperor) {
        long processingTime = emperorsBot.getDatabase().takeEmperor(user, chat, message.getText());
        String joining = "Set %s (Familiar name: %s) new emperor - %s - of group %s (Familiar name: %s). Took %sms.";
        emperorsBot.getLogger().info(String.format(joining, user.getId(),
                user.getUserName() != null ? user.getFirstName() + " | @" + user.getUserName() : user.getFirstName(),
                message.getText(), message.getChatId(), chat.getTitle(), processingTime));

        String photoUrl = "https://imgur.com/" + emperor.getPhotoId() + ".png";
        return String.format(Language.NEW_EMPEROR_OF_DAY.toString(), photoUrl, user.getFirstName(), emperor.getName());
    }
}
