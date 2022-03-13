package me.onlyfire.emperors.bot.commands.settings;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.commands.api.MessagedBotCommand;
import me.onlyfire.emperors.utils.MemberUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class UpdateSettingsCommand extends MessagedBotCommand {

    private final EmperorsBot emperorsBot;

    public UpdateSettingsCommand(EmperorsBot emperorsBot) {
        super("updatesettings", "Modifica le impostazioni del gruppo.");
        this.emperorsBot = emperorsBot;
    }

    @Override
    public void execute(AbsSender absSender, User user, Message message, Chat chat, String[] strings) {
        if (chat.isUserChat() || MemberUtils.isNormalUser(absSender, user, chat))
            return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));

        String key = strings[0];
        int value = toInt(strings[1], 1);

        if (value <= 0 || value > 300) {
            sendMessage.setText("Devi selezionare un valore compreso tra 1 e 300!");
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }

        emperorsBot.getDatabase().updateGroupSettings(chat.getId(), key, value).whenComplete((integer, throwable) -> {
            try {
                if (throwable != null) {
                    sendMessage.setText("Controlla di aver scritto bene chiave e valore!");
                    absSender.execute(sendMessage);
                    throwable.printStackTrace();
                    return;
                }
                sendMessage.setText("Aggiornato!");
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });
    }

    public int toInt(String in, int def) {
        if (in == null) {
            return def;
        } else {
            try {
                return Integer.parseInt(in);
            } catch (NumberFormatException var3) {
                return def;
            }
        }
    }

}