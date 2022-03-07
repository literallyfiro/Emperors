package me.onlyfire.emperors.bot.commands;

import me.onlyfire.emperors.bot.commands.api.MessagedBotCommand;
import me.onlyfire.emperors.Language;
import me.onlyfire.emperors.utils.InlineKeyboardBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StartCommand extends MessagedBotCommand {

    public StartCommand() {
        super("start", "Start");
    }

    @Override
    public void execute(AbsSender absSender, User user, Message message, Chat chat, String[] strings) {
        if (!chat.isUserChat()) return;

        SendMessage sendMessage = InlineKeyboardBuilder.create(chat.getId())
                .setText(Language.WELCOME.toString())
                .row()
                .button("Aggiungimi al tuo gruppo \uD83C\uDF7E", "invite_me_to_group", "https://telegram.me/EmperorsRobot?startgroup=true")
                .endRow()
                .row()
                .button("Canale Ufficiale", "official_channel", "https://t.me/emperorsbotchannel")
                .button("Sviluppatore", "contact_developer", "https://t.me/protocolsupport")
                .endRow()
                .build();
        try {
            absSender.executeAsync(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
