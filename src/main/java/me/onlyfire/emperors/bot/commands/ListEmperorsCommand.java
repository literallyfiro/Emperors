package me.onlyfire.emperors.bot.commands;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.commands.api.MessagedBotCommand;
import me.onlyfire.emperors.bot.EmperorException;
import me.onlyfire.emperors.bot.Emperor;
import me.onlyfire.emperors.bot.database.EmperorsDatabase;
import me.onlyfire.emperors.bot.Language;
import me.onlyfire.emperors.utils.Emoji;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

        EmperorsDatabase database = emperorsBot.getDatabase();
        database.getEmperors(chat.getId()).whenComplete(((emperors, exception) -> {
            if (exception != null) {
                emperorsBot.generateErrorMessage(chat, new EmperorException("Errore nel database", exception));
                return;
            }
            sendMessage.setText(emperors.isEmpty() ? Language.THERE_ARE_NO_EMPERORS.toString() : fetchEmperorList(chat.getTitle(), emperors));
            try {
                absSender.executeAsync(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }));

    }

    public String fetchEmperorList(String title, List<Emperor> emperors) {
        StringBuilder builder = new StringBuilder();
        AtomicInteger index = new AtomicInteger();

        builder.append(Emoji.CROWN).append(" • ").append("<b>Imperatori presenti in</b> <code>")
                .append(title).append("</code>").append("\n\n");

        emperors.stream()
                .sorted(Comparator.comparing(Emperor::getTakenByName, Comparator.nullsFirst(Comparator.reverseOrder()))
                        .thenComparing(Emperor::getName))
                .forEach(emp -> {
                    String name = emp.getName().substring(0, 1).toUpperCase() + emp.getName().substring(1);

                    builder.append("● ");

                    if (emp.getTakenByName() != null) {
                        builder.append("<strike>").append(name).append("</strike>").append(" [").append(emp.getTakenByName()).append("]");
                    } else {
                        builder.append(name);
                    }

                    if (index.incrementAndGet() != emperors.size()) {
                        builder.append("\n");
                    }
                });

        return builder.toString();
    }
}
