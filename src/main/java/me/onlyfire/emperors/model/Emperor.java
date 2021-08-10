package me.onlyfire.emperors.model;

import lombok.Data;

@Data
public class Emperor {

    private final String groupId;
    private final String name;
    private final String photoId;

    private String takenById;
    private String takenByName;
    private long takenTime;

}
