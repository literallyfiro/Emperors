package me.onlyfire.emperors.bot.exceptions;

public class EmperorException extends RuntimeException {

    public EmperorException(String message) {
        super(message);
    }

    public EmperorException(String message, Throwable cause) {
        super(message, cause);
    }
}
