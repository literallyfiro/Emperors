
CREATE TABLE IF NOT EXISTS `emperors`
(
    id          INTEGER AUTO_INCREMENT,
    groupId     LONG        NOT NULL,
    name        VARCHAR(15) NOT NULL,
    photoId     VARCHAR(15) NOT NULL,
    takenById   LONG DEFAULT NULL,
    takenByName TEXT DEFAULT NULL,
    takenTime   LONG,
    PRIMARY KEY (id)
);
