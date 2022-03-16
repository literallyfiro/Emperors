package me.onlyfire.emperors.cooldown;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CooldownManager {

    private static CooldownManager instance;
    private final Map<Long, Cooldown> inCooldown = new HashMap<>();

    public static CooldownManager getInstance() {
        if (instance == null)
            instance = new CooldownManager();
        return instance;
    }

    public void createCooldown(long userID, Chat chat, int time, TimeUnit unit) {
        inCooldown.remove(userID);
        inCooldown.put(userID, new Cooldown(userID, chat, unit.toMillis(time)));
    }

    public void removeCooldown(long userID, Chat chat) {
        inCooldown.values().stream().filter(col -> Objects.equals(col.getChat(), chat)).forEach(col -> inCooldown.remove(userID, col));
    }

    public boolean isInCooldown(long user, Chat chat) {
        Cooldown cooldown = getCooldown(user, chat);
        if (cooldown == null) return false;
        if (cooldown.isExpired()) {
            inCooldown.remove(user);
            return false;
        }
        return true;
    }

    public Cooldown getCooldown(long userID, Chat chat) {
        Optional<Cooldown> opt = inCooldown.values().stream().filter(col -> Objects.equals(col.getChat(), chat)).findFirst();
        return opt.orElseGet(() -> inCooldown.get(userID));
    }
}
