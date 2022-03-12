CREATE TABLE IF NOT EXISTS `emperors`
(
    `id`          INTEGER       AUTO_INCREMENT,
    `groupId`     BIGINT        NOT NULL,
    `name`        VARCHAR(15)   NOT NULL,
    `photoId`     VARCHAR(15)   NOT NULL,
    `takenById`   BIGINT        DEFAULT NULL,
    `takenByName` TEXT          DEFAULT NULL,
    `takenTime`   BIGINT,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS `settings`
(
    `groupId`               BIGINT      NOT NULL,
    `maxEmperorsPerUser`    INTEGER     DEFAULT 2,
    `emperorCooldown`       INTEGER     DEFAULT 10,
    PRIMARY KEY (groupId)
);
