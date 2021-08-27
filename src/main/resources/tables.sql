/*
 * Copyright (c) 2021.
 * The Emperors project is controlled by the GNU General Public License v3.0.
 * You can find it in the LICENSE file on the GitHub repository.
 */

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

CREATE TABLE IF NOT EXISTS `settings`
(
    groupId            LONG NOT NULL,
    maxEmperorsPerUser INTEGER DEFAULT 2
);

# uncomment this for next releases
#CREATE TABLE IF NOT EXISTS `suggestions` (id INTEGER NOT NULL AUTO_INCREMENT, fromChatId LONG NOT NULL, fromUserId LONG NOT NULL, fromUsername TEXT NOT NULL, message TEXT NOT NULL, PRIMARY KEY (id));