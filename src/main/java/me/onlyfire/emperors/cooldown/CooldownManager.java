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
    private final Map<User, Cooldown> inCooldown = new HashMap<>();

    public static CooldownManager getInstance() {
        if (instance == null)
            instance = new CooldownManager();
        return instance;
    }

    public void createCooldown(User user, Chat chat, int time, TimeUnit unit) {
        inCooldown.remove(user);
        inCooldown.put(user, new Cooldown(user, chat, unit.toMillis(time)));
    }

    public void removeCooldown(User user, Chat chat) {
        inCooldown.values().stream().filter(col -> Objects.equals(col.getChat(), chat)).forEach(col -> inCooldown.remove(user, col));
    }

    public boolean isInCooldown(User user, Chat chat) {
        Cooldown cooldown = getCooldown(user, chat);
        if (cooldown == null) return false;
        if (cooldown.isExpired()) {
            inCooldown.remove(user);
            return false;
        }
        return true;
    }

    public Cooldown getCooldown(User user, Chat chat) {
        Optional<Cooldown> opt = inCooldown.values().stream().filter(col -> Objects.equals(col.getChat(), chat)).findFirst();
        return opt.orElseGet(() -> inCooldown.get(user));
    }
}
