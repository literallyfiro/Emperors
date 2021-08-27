/*
 * Copyright (c) 2021.
 * The Emperors project is controlled by the GNU General Public License v3.0.
 * You can find it in the LICENSE file on the GitHub repository.
 */

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
