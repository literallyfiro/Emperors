package me.onlyfire.emperors.bot.mongo.models;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@Data
public class MongoUser {

    private ObjectId id = new ObjectId();

    @BsonProperty("user_id")
    private Long userId;

    @BsonProperty("username")
    private String userName;

    @BsonProperty("emperors_taken")
    private List<MongoTakenEmperor> emperorsTaken = new ArrayList<>();

}
