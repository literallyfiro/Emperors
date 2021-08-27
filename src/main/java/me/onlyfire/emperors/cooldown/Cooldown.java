/*
 * Copyright (c) 2021.
 * The Emperors project is controlled by the GNU General Public License v3.0.
 * You can find it in the LICENSE file on the GitHub repository.
 */

package me.onlyfire.emperors.cooldown;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.concurrent.TimeUnit;

public class Cooldown {
    @Getter
    private final User user;
    @Getter
    private final Chat chat;

    private final long length;
    private final long time;

    public Cooldown(User user, Chat chat, long length) {
        this.user = user;
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
