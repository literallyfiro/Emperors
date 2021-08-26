package me.onlyfire.emperors.bot.commands;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.commands.api.MessagedBotCommand;
import me.onlyfire.emperors.utils.Emoji;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class GlobalMessageCommand extends MessagedBotCommand {

    private final EmperorsBot emperorsBot;

    public GlobalMessageCommand(EmperorsBot emperorsBot) {
        super("global", "Invia un messaggio globale");
        this.emperorsBot = emperorsBot;
    }

    @Override
    public void execute(AbsSender absSender, User user, Message message, Chat chat, String[] strings) {
        if (!chat.isUserChat() && !user.getId().equals(339169693L))
            return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));
        sendMessage.setText(Emoji.HEAVY_CHECK_MARK + "<b>Inviato un messaggio globale a tutti i gruppi!</b>");
        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        sendMessage.setText("\uD83D\uDCAD <b>Messaggio globale:</b> " + String.join(" ", strings));
        emperorsBot.getChats().parallelStream().forEach(chatId -> {
            sendMessage.setChatId(String.valueOf(chatId));
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });

    }

}
