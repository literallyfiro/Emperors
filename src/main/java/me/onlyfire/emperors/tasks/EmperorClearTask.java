package me.onlyfire.emperors.tasks;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.database.Emperor;
import me.onlyfire.emperors.database.EmperorsDatabase;

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
