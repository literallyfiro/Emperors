package me.onlyfire.emperors.bot.listener.impl;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.listener.BotListener;
import me.onlyfire.emperors.essential.Language;
import me.onlyfire.emperors.model.Emperor;
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
        if (MemberUtils.isNormalUser(sender, user, chat))
            return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));
        sendMessage.setReplyToMessageId(message.getMessageId());

        if (!(emperorsBot.userMode.get(user) instanceof EmperorUserRemoval emperorUserRemoval))
            return;

        if (emperorUserRemoval.getChat().getId().equals(chat.getId()) && message.getReplyToMessage().equals(emperorUserRemoval.getMessage())) {
            if (!message.hasText())
                return;

            String emperorName = message.getText().toLowerCase();

            Emperor toRemove = emperorsBot.loadedEmperors
                    .stream()
                    .filter(emperor -> emperor.getName().equalsIgnoreCase(emperorName))
                    .filter(emperor -> emperor.getGroupId().equals(groupId))
                    .findFirst()
                    .orElse(null);

            if (toRemove == null) {
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
