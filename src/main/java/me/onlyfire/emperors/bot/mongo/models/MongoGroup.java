package me.onlyfire.emperors.bot.mongo.models;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@Data
public class MongoGroup {

    private ObjectId id = new ObjectId();

    @BsonProperty("group_id")
    private Long groupId;

    @BsonProperty("group_name")
    private String name;

    private List<MongoEmperor> emperors = new ArrayList<>();

    private MongoGroupSettings settings = new MongoGroupSettings();

}
