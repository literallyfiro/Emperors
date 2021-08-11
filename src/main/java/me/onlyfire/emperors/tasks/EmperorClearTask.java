package me.onlyfire.emperors.tasks;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.mongo.EmperorsMongoDatabase;
import me.onlyfire.emperors.bot.mongo.models.MongoEmperor;
import me.onlyfire.emperors.bot.mongo.models.MongoGroup;
import me.onlyfire.emperors.bot.mongo.models.MongoTakenEmperor;
import me.onlyfire.emperors.bot.mongo.models.MongoUser;

public record EmperorClearTask(EmperorsBot emperorsBot) implements Runnable {

    @Override
    public void run() {
        EmperorsMongoDatabase database = emperorsBot.getMongoDatabase();
        for (MongoUser users : database.getAllMongoUsers()) {
            for (MongoTakenEmperor takenEmperor : users.getEmperorsTaken()) {
                if (takenEmperor.getTakenTime() < (System.currentTimeMillis() / 1000)) {
                    users.getEmperorsTaken().remove(takenEmperor);
                    database.updateUser(users);
                    for (MongoGroup groups : database.getAllMongoGroups()) {
                        for (MongoEmperor emperor : groups.getEmperors()) {
                            emperor.setTaken(false);
                            database.updateGroup(groups);
                        }
                    }
                }
            }
        }
    }

}
