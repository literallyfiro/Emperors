package me.onlyfire.emperors.bot.database;

import org.intellij.lang.annotations.Language;

@SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection"})
public class Queries {

    @Language("MySQL")
    public static final String INSERT_EMPEROR = "INSERT INTO emperors (groupId, name, photoId) VALUES(?,?,?)";

    @Language("MySQL")
    public static final String UPDATE_EMPEROR = "UPDATE emperors SET takenById=?, takenByName=?, takenTime=? WHERE groupId=? AND name=?";

    @Language("MySQL")
    public static final String DELETE_EMPEROR = "DELETE FROM emperors WHERE name=? AND groupId=?";

    @Language("MySQL")
    public static final String SELECT_EMPEROR = "SELECT * FROM emperors WHERE groupId=? AND name=?";

    @Language("MySQL")
    public static final String CREATE_SETTINGS = "INSERT INTO settings (groupId) SELECT * FROM (SELECT ?) AS tmp WHERE NOT EXISTS (SELECT groupId FROM settings WHERE groupId = ?) LIMIT 1";

    @Language("MySQL")
    public static final String FETCH_SETTINGS = "SELECT * FROM settings WHERE groupId=?";

    @Language("MySQL")
    public static final String UPDATE_SETTINGS = "UPDATE settings SET %s=? WHERE groupId=?";

    @Language("MySQL")
    public static final String GET_EMPERORS_GROUP_ID = "SELECT * FROM emperors WHERE groupId=?";

    @Language("MySQL")
    public static final String FETCH_EMPERORS = "SELECT * FROM emperors";

}
