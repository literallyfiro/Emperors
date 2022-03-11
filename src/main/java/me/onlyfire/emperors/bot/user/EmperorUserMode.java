package me.onlyfire.emperors.bot.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.onlyfire.emperors.bot.EmperorsBot;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Getter
public abstract class EmperorUserMode {

    private final EmperorsBot emperorsBot;
    private final AbsSender sender;
    private final User user;
    private final Chat chat;
    private final Message message;

    private ScheduledExecutorService scheduledExecutor;

    public void start() {
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(() -> {
            if (!runCheck()) {
                scheduledExecutor.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduledExecutor.shutdown();
    }

    public abstract boolean runCheck();

    public abstract void completed(Message updatedMessage, String emperorName);
}
