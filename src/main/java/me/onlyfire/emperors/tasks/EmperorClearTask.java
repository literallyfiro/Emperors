package me.onlyfire.emperors.tasks;

import com.mongodb.client.MongoCursor;
import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.mongo.EmperorsMongoDatabase;
import me.onlyfire.emperors.bot.mongo.models.MongoUser;

public record EmperorClearTask(EmperorsBot emperorsBot) implements Runnable {

    @Override
    public void run() {
        EmperorsMongoDatabase database = emperorsBot().getMongoDatabase();
        MongoCursor<MongoUser> users = database.getAllMongoUsers();
        while (users.hasNext()) {
            MongoUser currentUser = users.next();
            currentUser.getEmperorsTaken().removeIf(currentEmperor -> currentEmperor.getTakenTime() < (System.currentTimeMillis() / 1000));
            database.updateUser(currentUser);
        }
    }

}
