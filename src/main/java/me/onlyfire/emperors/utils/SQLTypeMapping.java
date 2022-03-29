package me.onlyfire.emperors.utils;

import lombok.experimental.UtilityClass;

import java.sql.Types;

@UtilityClass
public class SQLTypeMapping {

    /**
     * Translates a data type from an integer (java.sql.Types value) to a string
     * that represents the corresponding class.
     *
     * REFER: https://www.cis.upenn.edu/~bcpierce/courses/629/jdkdocs/guide/jdbc/getstart/mapping.doc.html
     *
     * @param type
     *            The java.sql.Types value to convert to its corresponding class.
     * @return The class that corresponds to the given java.sql.Types
     *         value, or Object if the type has no known mapping.
     */
    public static Class<?> toClass(int type) {
        return switch (type) {
            case Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR -> String.class;
            case Types.NUMERIC, Types.DECIMAL -> java.math.BigDecimal.class;
            case Types.BIT -> Boolean.class;
            case Types.TINYINT -> Byte.class;
            case Types.SMALLINT -> Short.class;
            case Types.INTEGER -> Integer.class;
            case Types.BIGINT -> Long.class;
            case Types.REAL, Types.FLOAT -> Float.class;
            case Types.DOUBLE -> Double.class;
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> Byte[].class;
            case Types.DATE -> java.sql.Date.class;
            case Types.TIME -> java.sql.Time.class;
            case Types.TIMESTAMP -> java.sql.Timestamp.class;
            default -> Object.class;
        };
    }

}
