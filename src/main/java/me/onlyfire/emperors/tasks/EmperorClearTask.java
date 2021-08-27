/*
 * Copyright (c) 2021.
 * The Emperors project is controlled by the GNU General Public License v3.0.
 * You can find it in the LICENSE file on the GitHub repository.
 */

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
