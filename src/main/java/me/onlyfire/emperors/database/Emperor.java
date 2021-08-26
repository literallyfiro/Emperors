package me.onlyfire.emperors.database;

import lombok.Data;

@Data
public class Emperor {

    private final long groupId;
    private final String name;
    private final String photoId;
    private final long takenById;
    private final String takenByName;
    private final long takenTime;

}
