package me.onlyfire.emperors.bot.commands;

import me.onlyfire.emperors.bot.commands.api.MessagedBotCommand;
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
                .setText("""
                        <b>Ciao! Benvenuto in @EmperorsRobot!</b>
                                                
                        👑 <b>Cosa è Emperors?</b>
                        Emperors è un bot per far divertire gli utenti del vostro gruppo.
                        Scrivendo il nome di un imperatore, i vostri utenti potranno diventare l'imperatore del giorno.
                                                
                        ❔ <b>Come funziona?</b>
                        Gli amministratori del gruppo avranno il permesso di creare un imperatore (e di rimuoverlo), inserendo una foto e il suo identificativo (che gli utenti dovranno scrivere per diventare l'imperatore del giorno)
                                                
                        ⚡️ <b>Quanti imperatori si possono aggiungere in un gruppo?</b>
                        La risposta è semplice. Infiniti
                        Sbizzarritevi a creare i vostri imperatori senza nessun tipo di limite!
                                         
                         
                        ⚠️ <b>Nota, il bot è in uno stato chiamato BETA, ciò significa che non è la versione finale e ci potranno essere vari cambiamenti</b>
                        """
                )
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
