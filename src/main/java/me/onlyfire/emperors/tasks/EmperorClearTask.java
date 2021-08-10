package me.onlyfire.emperors.tasks;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.essential.Database;

public record EmperorClearTask(EmperorsBot emperorsBot) implements Runnable {

    @Override
    public void run() {
        emperorsBot.loadedEmperors.forEach(emperor -> {
            if (emperor.getTakenTime() < (System.currentTimeMillis() / 1000)) {
                emperor.setTakenById(null);
                emperor.setTakenByName(null);
                emperor.setTakenTime(0L);

                Database.executeUpdate("UPDATE emperors SET takenBy=?, takenByName=?, takenTime=? WHERE groupId = ? AND name = ? ",
                        new Object[]{null, null, 0L, emperor.getGroupId(), emperor.getName()});
            }
        });
    }

}
