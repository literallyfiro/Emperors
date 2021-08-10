package me.onlyfire.emperors.bot.commands;

import me.onlyfire.emperors.bot.commands.api.MessagedBotCommand;
import me.onlyfire.emperors.utils.AdminPanelUtils;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class AdminCommand extends MessagedBotCommand {

    public AdminCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Message message, Chat chat, String[] strings) {
        if (!chat.isUserChat() && !user.getId().equals(339169693L))
            return;

        AdminPanelUtils.sendMainPanel(absSender, user, chat.getId());
    }
}
