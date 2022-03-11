package me.onlyfire.emperors.bot.listener.impl;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.EmperorException;
import me.onlyfire.emperors.bot.listener.BotListener;
import me.onlyfire.emperors.bot.database.EmperorsDatabase;
import me.onlyfire.emperors.bot.Language;
import me.onlyfire.emperors.bot.user.impl.EmperorUserCreation;
import me.onlyfire.emperors.utils.MemberUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public record AddEmperorListener(EmperorsBot emperorsBot) implements BotListener {

    @Override
    public void execute(Update update, AbsSender sender) {
        Message message = update.getMessage();

        if (message == null) return;

        Chat chat = message.getChat();
        User user = message.getFrom();
        if (MemberUtils.isNormalUser(sender, user, chat)) return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));
        sendMessage.setReplyToMessageId(message.getMessageId());

        if (!(emperorsBot.getUserMode().get(user) instanceof EmperorUserCreation emperorUserCreation)) return;

        if (emperorUserCreation.getChat().getId().equals(chat.getId())) {
            if (message.hasPhoto()) {
                sendMessage.setText("Devi mandare una foto SENZA usare la compressione di telegram!");
                try {
                    sender.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (!message.hasDocument() || message.getCaption() == null || message.getCaption().isEmpty()) return;

            String emperorName = message.getCaption().toLowerCase();
            EmperorsDatabase database = emperorsBot.getDatabase();
            database.getEmperor(message.getChatId(), emperorName).whenComplete((emperor, exception) -> {
                if (exception != null) {
                    emperorsBot.generateErrorMessage(chat, new EmperorException("Errore nel database", exception));
                    return;
                }
                if (emperor != null) {
                    sendMessage.setText(Language.ALREADY_EXIST_EMPEROR.toString());
                    try {
                        sender.executeAsync(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                if (emperor == null) emperorUserCreation.completed(message, emperorName);
                else emperorsBot.removeUserMode(user, chat, null);
            });

        }
    }
}
