package me.onlyfire.emperors.bot.commands.settings;

import me.onlyfire.emperors.bot.EmperorException;
import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.Settings;
import me.onlyfire.emperors.bot.commands.api.MessagedBotCommand;
import me.onlyfire.emperors.utils.Emoji;
import me.onlyfire.emperors.utils.MemberUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class GetSettingsCommand extends MessagedBotCommand {

    private final EmperorsBot emperorsBot;

    public GetSettingsCommand(EmperorsBot emperorsBot) {
        super("getsettings", "Controlla le impostazioni del gruppo.");
        this.emperorsBot = emperorsBot;
    }

    @Override
    public void execute(AbsSender absSender, User user, Message message, Chat chat, String[] strings) {
        if (chat.isUserChat() || MemberUtils.isNormalUser(absSender, user, chat))
            return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));

        emperorsBot.getDatabase().getGroupSettings(chat.getId()).whenComplete((settings, throwable) -> {
            if (throwable != null) {
                emperorsBot.generateErrorMessage(chat, new EmperorException("Errore nel database", throwable));
                return;
            }
            sendMessage.setText(fetchSettingsValues(chat.getTitle(), settings));
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });
    }

    public String fetchSettingsValues(String title, Settings settings) {
        return Emoji.CLOUD + " • " + "<b>Impostazioni di </b> <code>" +
                title + "</code>" + "\n\n" +
                "- " + "<code>maxEmperorsPerUser</code> <i>(int)</i>: " + settings.getMaxEmperorsPerUser() + "\n" +
                "\n" + "<b>Per modificare le impostazioni del gruppo, esegui /updatesettings [key] [valore] </b>";
    }
}
