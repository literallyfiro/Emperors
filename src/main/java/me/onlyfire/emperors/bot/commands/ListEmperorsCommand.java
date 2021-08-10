package me.onlyfire.emperors.bot.commands;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.commands.api.MessagedBotCommand;
import me.onlyfire.emperors.model.Emperor;
import me.onlyfire.emperors.utils.Emoji;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

        if (emperorsBot.loadedEmperors.isEmpty() || areEmperorsEmpty(chat)) {
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
        List<Emperor> emperorList = emperorsBot.loadedEmperors
                .stream()
                .filter(emp -> emp.getGroupId().equals(String.valueOf(chat.getId())))
                .sorted(Comparator.comparing(Emperor::getTakenById, Comparator.nullsFirst(Comparator.reverseOrder()))
                        .thenComparing(Emperor::getName))
                .collect(Collectors.toList());

        emperorList.forEach(emperor -> {
            String name = emperor.getName().substring(0, 1).toUpperCase() + emperor.getName().substring(1);
            builder.append("● ").append(emperor.getTakenById() == null ? name : "<strike>" + name + "</strike>");
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

    public boolean areEmperorsEmpty(Chat chat) {
        return emperorsBot.loadedEmperors
                .stream()
                .map(Emperor::getGroupId)
                .noneMatch(id -> StringUtils.equals(id, String.valueOf(chat.getId())));
    }
}
