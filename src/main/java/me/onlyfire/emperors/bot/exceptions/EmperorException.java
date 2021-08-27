/*
 * Copyright (c) 2021.
 * The Emperors project is controlled by the GNU General Public License v3.0.
 * You can find it in the LICENSE file on the GitHub repository.
 */

package me.onlyfire.emperors.bot.exceptions;

public class EmperorException extends RuntimeException {

    public EmperorException(String message) {
        super(message);
    }

    public EmperorException(String message, Throwable cause) {
        super(message, cause);
    }
}
