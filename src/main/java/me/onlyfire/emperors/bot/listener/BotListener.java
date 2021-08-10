package me.onlyfire.emperors.bot.listener;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface BotListener {

    void execute(Update update, AbsSender sender);

}
