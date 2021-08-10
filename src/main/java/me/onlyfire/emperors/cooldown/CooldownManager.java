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
    private final Map<User, Cooldown> cooldowns = new HashMap<>();

    public static CooldownManager getInstance() {
        if (instance == null)
            instance = new CooldownManager();
        return instance;
    }

    public void createCooldown(User user, Chat chat, int time, TimeUnit unit) {
        cooldowns.remove(user);
        cooldowns.put(user, new Cooldown(user, chat, unit.toMillis(time)));
    }

    public void removeCooldown(User user, Chat chat) {
        cooldowns.values().stream().filter(col -> Objects.equals(col.getChat(), chat)).forEach(col -> cooldowns.remove(user, col));
    }

    public boolean isInCooldown(User user, Chat chat) {
        Cooldown cooldown = getCooldown(user, chat);
        if (cooldown == null) return false;
        if (cooldown.isExpired()) {
            cooldowns.remove(user);
            return false;
        }
        return true;
    }

    public Cooldown getCooldown(User user, Chat chat) {
        Optional<Cooldown> opt = cooldowns.values().stream().filter(col -> Objects.equals(col.getChat(), chat)).findFirst();
        return opt.orElseGet(() -> cooldowns.get(user));
    }
}
