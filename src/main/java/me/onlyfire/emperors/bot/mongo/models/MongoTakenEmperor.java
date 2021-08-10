package me.onlyfire.emperors.bot.mongo.models;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Data
public class MongoTakenEmperor {

    private ObjectId id = new ObjectId();

    private String name;

    @BsonProperty("group_id")
    private Long groupId;

    @BsonProperty("taken_time")
    private Long takenTime;

    @BsonProperty("taken_by_id")
    private Long takenById;

    @BsonProperty("taken_by_name")
    private String takenByName;
}
