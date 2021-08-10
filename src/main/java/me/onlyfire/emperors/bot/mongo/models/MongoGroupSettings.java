package me.onlyfire.emperors.bot.mongo.models;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Data
public class MongoGroupSettings {

    @BsonProperty("max_emperors_per_user")
    private Integer maxEmperorsPerUser = 5;

}
