package me.onlyfire.emperors.bot.commands;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.commands.api.MessagedBotCommand;
import me.onlyfire.emperors.essential.Language;
import me.onlyfire.emperors.user.impl.EmperorUserRemoval;
import me.onlyfire.emperors.utils.MemberUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class RemoveEmperorCommand extends MessagedBotCommand {

    private final EmperorsBot emperorsBot;

    public RemoveEmperorCommand(EmperorsBot emperorsBot) {
        super("removeemperor", "Rimuove un imperatore dal gruppo");
        this.emperorsBot = emperorsBot;
    }

    @Override
    public void execute(AbsSender absSender, User user, Message message, Chat chat, String[] strings) {
        if (chat.isUserChat() || MemberUtils.isNormalUser(absSender, user, chat))
            return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.setReplyToMessageId(message.getMessageId());

        if (emperorsBot.userMode.containsKey(user)) {
            emperorsBot.removeUserMode(user, chat, null);
            sendMessage.setText("Sei stato rimosso dalla precedente sessione.");
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        sendMessage.setText(Language.REMOVE_EMPEROR_FIRST_STEP.toString());
        ForceReplyKeyboard kb = ForceReplyKeyboard
                .builder()
                .inputFieldPlaceholder("Inserisci un nome")
                .forceReply(true)
                .selective(true)
                .build();
        sendMessage.setReplyMarkup(kb);

        EmperorUserRemoval emperorUserRemoval = new EmperorUserRemoval(emperorsBot, absSender, user, chat, message);
        try {
            absSender.execute(sendMessage);
            emperorsBot.userMode.put(user, emperorUserRemoval);
            emperorUserRemoval.start();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
