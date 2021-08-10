package me.onlyfire.emperors.bot.commands;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.commands.api.MessagedBotCommand;
import me.onlyfire.emperors.bot.exceptions.EmperorException;
import me.onlyfire.emperors.bot.mongo.EmperorsMongoDatabase;
import me.onlyfire.emperors.bot.mongo.models.MongoEmperor;
import me.onlyfire.emperors.bot.mongo.models.MongoGroup;
import me.onlyfire.emperors.bot.mongo.models.MongoTakenEmperor;
import me.onlyfire.emperors.utils.Emoji;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ListEmperorsCommand extends MessagedBotCommand {

    private final EmperorsBot emperorsBot;

    public ListEmperorsCommand(EmperorsBot emperorsBot) {
        super("listemperors", "Guarda la lista degli imperatori presenti in questo gruppo.");
        this.emperorsBot = emperorsBot;
    }

    @Override
    public void execute(AbsSender absSender, User user, Message message, Chat chat, String[] strings) {
        if (chat.isUserChat()) return;

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(String.valueOf(chat.getId()));

        EmperorsMongoDatabase database = emperorsBot.getMongoDatabase();
        MongoGroup mongoGroup = database.getMongoGroup(chat);
        if (mongoGroup == null)
            throw new EmperorException("Could not fetch group id " + message.getChatId());

        List<MongoEmperor> emperorList = mongoGroup.getEmperors();

        if (emperorList.isEmpty()) {
            sendMessage.setText("<b>Non ci sono imperatori in questo gruppo!</b> " + Emoji.CRYING_FACE);
            try {
                absSender.executeAsync(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(Emoji.CROWN).append(" • ").append("<b>Imperatori presenti in</b> <code>")
                .append(chat.getTitle()).append("</code>").append("\n\n");

        AtomicInteger index = new AtomicInteger();

        emperorList
                .stream()
                .sorted(Comparator.comparing(MongoEmperor::isTaken, Comparator.naturalOrder())
                        .thenComparing(MongoEmperor::getName))
                .forEach(emp -> {
                    String name = emp.getName().substring(0, 1).toUpperCase() + emp.getName().substring(1);
                    builder.append("● ").append(emp.isTaken() ? "<strike>" + name + "</strike>" : name);
                    if (index.incrementAndGet() != emperorList.size()) {
                        builder.append("\n");
                    }
                });

        sendMessage.setText(builder.toString());

        try {
            absSender.executeAsync(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
