/*
 * Copyright (c) 2021.
 * The Emperors project is controlled by the GNU General Public License v3.0.
 * You can find it in the LICENSE file on the GitHub repository.
 */

package me.onlyfire.emperors.essential;

import lombok.Data;

/**
 * Class file for storing bot variables, loaded from a <b>configuration file </b>.
 *
 * @author firo
 * @see me.onlyfire.emperors.Bot
 * @since 1.0
 */
@Data
public class BotVars {
    private final String token, username, uri, trelloKey, trelloAccessToken, imgur;
}
