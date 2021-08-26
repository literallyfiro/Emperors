package me.onlyfire.emperors.bot.listener.impl;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.listener.BotListener;
import me.onlyfire.emperors.essential.Database;
import me.onlyfire.emperors.essential.Language;
import me.onlyfire.emperors.model.Emperor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;

public record UserEmperorListener(EmperorsBot emperorsBot) implements BotListener {

    @Override
    public void execute(Update update, AbsSender sender) {
        Message message = update.getMessage();

        if (message == null)
            return;

        Chat chat = message.getChat();
        String groupId = String.valueOf(message.getChatId());
        User user = message.getFrom();

        if (emperorsBot.userMode.containsKey(user))
            return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));
        sendMessage.setReplyToMessageId(message.getMessageId());

        for (Emperor emperor : emperorsBot.loadedEmperors) {
            if (message.hasText() && message.getText().equalsIgnoreCase(emperor.getName()) && emperor.getGroupId().equals(groupId)) {
                if (emperor.getTakenById() == null) {

                    ZoneId zoneId = ZoneId.of("Europe/Rome");
                    ZonedDateTime startDateTime = ZonedDateTime.ofInstant(Instant.now(), zoneId).toLocalDate().atStartOfDay(zoneId);
                    ZonedDateTime tomorrowDateTime = startDateTime.plusDays(1);

                    String takenBy = String.valueOf(user.getId());
                    long takenTime = tomorrowDateTime.toEpochSecond();

                    CompletableFuture<Integer> future = Database.executeUpdate("UPDATE emperors SET takenBy = ?, takenByName = ?, takenTime = ? WHERE groupId = ? AND name = ?",
                            new Object[]{takenBy, user.getFirstName(), takenTime, groupId, message.getText()});
                    long before = System.currentTimeMillis();
                    future.whenComplete(((integer, throwable) -> {
                        if (throwable != null) {
                            throwable.printStackTrace();
                            return;
                        }

                        emperor.setTakenById(takenBy);
                        emperor.setTakenByName(user.getFirstName());
                        emperor.setTakenTime(takenTime);

                        long after = (System.currentTimeMillis() - before);
                        String joining = "Set %s (Familiar name: %s) new emperor - %s - of group %s (Familiar name: %s). Took %sms.";
                        emperorsBot.LOGGER.info(String.format(joining, user.getId(),
                                user.getUserName() != null ? user.getFirstName() + " | @" + user.getUserName() : user.getFirstName(),
                                emperor.getName(), groupId, chat.getTitle(), after));

                        String photoUrl = "https://imgur.com/" + emperor.getPhotoId() + ".png";
                        sendMessage.setText(String.format(Language.NEW_EMPEROR_OF_DAY.toString(),
                                photoUrl, user.getFirstName(), emperor.getName()));
                        try {
                            sender.execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }));
                } else {
                    if (emperor.getTakenById().equals(String.valueOf(user.getId()))) {
                        sendMessage.setText(Language.ALREADY_HAS_EMPEROR_SELF.toString());
                        try {
                            sender.execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    sendMessage.setText(String.format(Language.ALREADY_HAS_EMPEROR.toString(), emperor.getTakenByName(), emperor.getName()));
                    try {
                        sender.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
