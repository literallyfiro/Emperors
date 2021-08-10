package me.onlyfire.emperors.bot.mongo.models;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Data
public class MongoSuggestion {

    private ObjectId id = new ObjectId();

    @BsonProperty("from_user")
    private Long fromUser;

    @BsonProperty("from_chat")
    private Long fromChat;

    private String message;

}
