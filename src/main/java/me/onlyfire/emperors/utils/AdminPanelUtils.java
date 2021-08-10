package me.onlyfire.emperors.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@UtilityClass
public final class AdminPanelUtils {

    public static void sendMainPanel(AbsSender absSender, User user, Long chatId) {
        SendMessage sendMessage = InlineKeyboardBuilder.create(chatId)
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
                .row()
                .button("Invia messaggio globale \uD83C\uDF10", "send_global_message", null)
                .endRow()
                .build();
        try {
            absSender.executeAsync(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
