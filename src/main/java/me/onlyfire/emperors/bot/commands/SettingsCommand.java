/*
 * Copyright (c) 2021.
 * The Emperors project is controlled by the GNU General Public License v3.0.
 * You can find it in the LICENSE file on the GitHub repository.
 */

package me.onlyfire.emperors.bot.commands;

import me.onlyfire.emperors.bot.commands.api.MessagedBotCommand;
import me.onlyfire.emperors.essential.Language;
import me.onlyfire.emperors.utils.InlineKeyboardBuilder;
import me.onlyfire.emperors.utils.MemberUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SettingsCommand extends MessagedBotCommand {

    public SettingsCommand() {
        super("settings", "Gestisci le impostazioni del gruppo");
    }

    @Override
    public void execute(AbsSender absSender, User user, Message message, Chat chat, String[] strings) {
        if (chat.isUserChat() || MemberUtils.isNormalUser(absSender, user, chat)) return;

        SendMessage sendMessage = InlineKeyboardBuilder.create(chat.getId())
                .setText(String.format(Language.SETTINGS.toString(), chat.getTitle()))
                .row()
                .button("Imperatori massimi per utente 👤", "max_emperors_per_user", null)
                .endRow()
                .row()
                .button("\uD83D\uDED1 Esci", "exit", null)
                .endRow()
                .build();
        try {
            absSender.executeAsync(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
