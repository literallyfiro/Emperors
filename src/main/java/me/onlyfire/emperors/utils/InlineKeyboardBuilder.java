package me.onlyfire.emperors.utils;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class InlineKeyboardBuilder {

    private final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
    private Long chatId;
    private String text;
    private List<InlineKeyboardButton> row = null;

    private InlineKeyboardBuilder() {
    }

    public static InlineKeyboardBuilder create(Long chatId) {
        InlineKeyboardBuilder builder = new InlineKeyboardBuilder();
        builder.setChatId(chatId);
        return builder;
    }

    public InlineKeyboardBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public InlineKeyboardBuilder row() {
        this.row = new ArrayList<>();
        return this;
    }

    public InlineKeyboardBuilder button(String text, String callbackData, String url) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        if (url != null) {
            button.setUrl(url);
        }
        row.add(button);
        return this;
    }

    public InlineKeyboardBuilder endRow() {
        this.keyboard.add(this.row);
        this.row = null;
        return this;
    }


    public SendMessage build() {
        SendMessage message = new SendMessage();

        message.setChatId(String.valueOf(chatId));
        message.enableHtml(true);
        message.setText(text);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        return message;
    }

}


