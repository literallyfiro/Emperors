package me.onlyfire.emperors.bot.mongo.models;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Data
public class MongoEmperor {

    private ObjectId id = new ObjectId();

    private String name;

    @BsonProperty("photo_id")
    private String photoId;

    private boolean taken = false;

}
