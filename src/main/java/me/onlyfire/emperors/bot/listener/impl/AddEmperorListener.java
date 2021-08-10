package me.onlyfire.emperors.bot.listener.impl;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.listener.BotListener;
import me.onlyfire.emperors.essential.Language;
import me.onlyfire.emperors.model.Emperor;
import me.onlyfire.emperors.user.impl.EmperorUserCreation;
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

        if (!(emperorsBot.userMode.get(user) instanceof EmperorUserCreation emperorUserCreation))
            return;

        if (emperorUserCreation.getChat().getId().equals(chat.getId()) && message.getReplyToMessage().equals(emperorUserCreation.getMessage())) {
            if (message.hasPhoto()) {
                sendMessage.setText("Devi mandare una foto SENZA usare la compressione di telegram!");
                try {
                    sender.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (!message.hasDocument() || message.getCaption() == null || message.getCaption().isEmpty())
                return;

            String emperorName = message.getCaption().toLowerCase();

            Emperor toAdd = emperorsBot.loadedEmperors
                    .stream()
                    .filter(emperor -> emperor.getName().equalsIgnoreCase(emperorName))
                    .filter(emperor -> emperor.getGroupId().equals(groupId))
                    .findFirst()
                    .orElse(null);

            if (toAdd != null) {
                sendMessage.setText(Language.ALREADY_EXIST_EMPEROR.toString());
                try {
                    sender.executeAsync(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                emperorsBot.removeUserMode(user, chat, null);
                return;
            }

//            String documentName = message.getDocument().getFileName();
//            if (!documentName.endsWith(".png") && !documentName.endsWith(".jpg")) {
//                sendMessage.setText("Mi dispiace, ma i formati supportati sono solo <code>png</code> e <code>jpg</code>. " +
//                        "Tutti gli altri formati (tra cui video/gif) NON sono supportati attualmente! Sei stato rimosso dalla modalità creazione.");
//
//                emperorsBot.removeUserMode(user, chat, null);
//                return;
//            }

            sendMessage.setText(Language.CREATION_IN_PROGRESS.toString());
            try {
                sender.executeAsync(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            emperorUserCreation.completed(message, emperorName);
        }
    }
}
