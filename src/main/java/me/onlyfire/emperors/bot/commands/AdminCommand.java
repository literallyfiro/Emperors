/*
 * Copyright (c) 2021.
 * The Emperors project is controlled by the GNU General Public License v3.0.
 * You can find it in the LICENSE file on the GitHub repository.
 */

package me.onlyfire.emperors.bot.commands;

import me.onlyfire.emperors.bot.commands.api.MessagedBotCommand;
import me.onlyfire.emperors.utils.InlineKeyboardBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class AdminCommand extends MessagedBotCommand {

    public AdminCommand() {
        super("admin", "Pannello di amministrazione");
    }

    @Override
    public void execute(AbsSender absSender, User user, Message message, Chat chat, String[] strings) {
        if (!chat.isUserChat() && !user.getId().equals(339169693L))
            return;

        SendMessage sendMessage = InlineKeyboardBuilder.create(chat.getId())
                .setText("\uD83D\uDEE0 <b>Pannello di Amministrazione</b> \uD83D\uDEE0\n\n" +
                        "Benvenuto " + user.getFirstName() + " nel pannello di amministrazione di @EmperorsBot.\n" +
                        "Qui potrai modificare varie impostazioni del bot, tra cui stopparlo o fare un nuovo update.\n\n" +
                        "<b>Il tuo id</b> » " + user.getId()
                )
                .row()
                .button("Nuovo Update \uD83C\uDFF3", "new_update_bot", null)
                .endRow()
                .row()
                .button("Stoppa il bot \uD83D\uDEA8", "stop_bot", null)
                .button("Riavvia il bot \uD83D\uDDEF", "restart_bot", null)
                .endRow()
                .build();
        try {
            absSender.executeAsync(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
