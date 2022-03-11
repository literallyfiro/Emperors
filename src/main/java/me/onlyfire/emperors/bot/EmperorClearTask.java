package me.onlyfire.emperors.bot;

import me.onlyfire.emperors.bot.database.EmperorsDatabase;

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
