package me.onlyfire.emperors.user.impl;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.exceptions.EmperorException;
import me.onlyfire.emperors.essential.Database;
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
import java.util.concurrent.CompletableFuture;
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
            emperorsBot.userMode.remove(user);
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

        CompletableFuture<Integer> future = Database.executeUpdate("DELETE FROM emperors WHERE name = ? AND groupId = ?;",
                new Object[]{emperorName, chat.getId()});
        long before = System.currentTimeMillis();
        future.whenComplete(((integer, futureThrowable) -> {
            if (futureThrowable != null) {
                emperorsBot.removeUserMode(user, chat, new EmperorException("Errore durante la rimozione del record", futureThrowable));
                return;
            }
            emperorsBot.loadedEmperors.removeIf(emperor -> emperor.getName().equalsIgnoreCase(emperorName) && emperor.getGroupId().equals(String.valueOf(chat.getId())));
            emperorsBot.removeUserMode(user, chat, null);

            long after = (System.currentTimeMillis() - before);
            String joining = "Removed emperor %s on group %s (Familiar name: %s). Took %sms for completion.";
            emperorsBot.LOGGER.info(String.format(joining, emperorName, chat.getId(), chat.getTitle(), after));

            try {
                sender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }));
    }

    public Chat getChat() {
        return chat;
    }
}
