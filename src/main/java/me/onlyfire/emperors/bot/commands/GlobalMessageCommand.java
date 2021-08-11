package me.onlyfire.emperors.bot.commands;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.commands.api.MessagedBotCommand;
import me.onlyfire.emperors.bot.mongo.models.MongoGroup;
import me.onlyfire.emperors.essential.Language;
import me.onlyfire.emperors.user.impl.EmperorUserCreation;
import me.onlyfire.emperors.utils.MemberUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
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
//        sendMessage.setChatId(String.valueOf(chat.getId()));
//        sendMessage.setText("Inviato un messaggio globale a tutti i gruppi!");
//        try {
//            absSender.execute(sendMessage);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }

        sendMessage.setText("\uD83D\uDCAD <b>Messaggio globale:</b> " + String.join(" ", strings));
        for (MongoGroup mongoGroup : emperorsBot.getMongoDatabase().getAllMongoGroups()) {
            System.out.println("DEBUG ## " + mongoGroup.getGroupId() + " - " + mongoGroup.getName());
            sendMessage.setChatId(String.valueOf(mongoGroup.getGroupId()));
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

}
