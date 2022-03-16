package me.onlyfire.emperors.cooldown;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.util.concurrent.TimeUnit;

public class Cooldown {
    @Getter
    private final long userID;
    @Getter
    private final Chat chat;

    private final long length;
    private final long time;

    public Cooldown(long userID, Chat chat, long length) {
        this.userID = userID;
        this.chat = chat;
        this.length = length;
        this.time = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= time + length;
    }

    public long getTimeRemaining() {
        return TimeUnit.MILLISECONDS.toSeconds((time + length) - System.currentTimeMillis());
    }

}
