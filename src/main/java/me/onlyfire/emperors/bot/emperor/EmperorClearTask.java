package me.onlyfire.emperors.bot.emperor;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.emperor.Emperor;
import me.onlyfire.emperors.bot.emperor.EmperorsDatabase;

public record EmperorClearTask(EmperorsBot emperorsBot) implements Runnable {

    @Override
    public void run() {
        EmperorsDatabase database = emperorsBot.getDatabase();
        database.getEmperors().whenComplete((emperors, throwable) -> {
            for (Emperor emperor : emperors) {
                if (emperor.getTakenTime() < (System.currentTimeMillis() / 1000)) {
                    database.emitEmperor(emperor);
                }
            }
        });
    }

}
