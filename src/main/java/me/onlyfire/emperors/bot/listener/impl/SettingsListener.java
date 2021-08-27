/*
 * Copyright (c) 2021.
 * The Emperors project is controlled by the GNU General Public License v3.0.
 * You can find it in the LICENSE file on the GitHub repository.
 */

package me.onlyfire.emperors.bot.listener.impl;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.listener.BotListener;
import me.onlyfire.emperors.essential.Language;
import me.onlyfire.emperors.utils.InlineKeyboardBuilder;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public record SettingsListener(EmperorsBot emperorsBot) implements BotListener {

    @Override
    public void execute(Update update, AbsSender sender) {
        if (!update.hasCallbackQuery()) return;
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        if (data == null) return;

        Long groupId = callbackQuery.getMessage().getChatId();
        String argument = data.substring(data.lastIndexOf("_") + 1);
        boolean remove = argument.startsWith("0");
        System.out.println(remove);

        emperorsBot.getDatabase().getGroupSettings(groupId).whenComplete(((settings, throwable) -> {
            if (settings == null) {
                emperorsBot.getLogger().warn("Settings object is null");
                return;
            }
            if (throwable != null) {
                emperorsBot.getLogger().error("Throwable while getting group settings", throwable);
                return;
            }

            switch (data) {
                case "max_emperors_per_user" -> reloadMaxEmperorsPage(groupId, callbackQuery.getMessage().getMessageId(),
                        sender, settings.getMaxEmperorsPerUser());
                case "go_back_to_monke" -> goBack(groupId, callbackQuery.getMessage().getChat().getTitle(),
                        callbackQuery.getMessage().getMessageId(), sender);
                case "exit" -> {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(String.valueOf(groupId));
                    deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());
                    try {
                        sender.executeAsync(deleteMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (settings.getMaxEmperorsPerUser() < 2 && remove) {
                return;
            }

            int i = Integer.parseInt(argument);
            int newValue = remove ? settings.getMaxEmperorsPerUser() - i : settings.getMaxEmperorsPerUser() + i;
            emperorsBot.getDatabase().setGroupSettings(groupId, "maxEmperorsPerUser", newValue);
            reloadMaxEmperorsPage(groupId, callbackQuery.getMessage().getMessageId(), sender, newValue);
        }));
    }

    private void reloadMaxEmperorsPage(Long groupId, Integer messageId, AbsSender sender, int maxEmperorsPerUser) {
        try {
            sender.executeAsync(InlineKeyboardBuilder.create(groupId)
                    .setMessageId(messageId)
                    .setText(String.format(Language.SETTINGS_SPECIFIC.toString(),
                            "\uD83D\uDC64 Imperatori massimi per utente", maxEmperorsPerUser))
                    .row()
                    .button("+1", "max_emperors_per_user_1", null)
                    .button("+2", "max_emperors_per_user_2", null)
                    .button("+5", "max_emperors_per_user_5", null)
                    .button("+10", "max_emperors_per_user_10", null)
                    .endRow()
                    .row()
                    .button("-1", "max_emperors_per_user_01", null)
                    .button("-2", "max_emperors_per_user_02", null)
                    .button("-5", "max_emperors_per_user_05", null)
                    .button("-10", "max_emperors_per_user_010", null)
                    .endRow()
                    .row()
                    .button("\uD83D\uDD19 Indietro", "go_back_to_monke", null)
                    .endRow()
                    .buildEditMessage());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void goBack(Long groupId, String title, Integer messageId, AbsSender sender) {
        try {
            sender.executeAsync(InlineKeyboardBuilder.create(groupId)
                    .setMessageId(messageId)
                    .setText(String.format(Language.SETTINGS.toString(), title))
                    .row()
                    .button("Imperatori massimi per utente 👤", "max_emperors_per_user", null)
                    .endRow()
                    .row()
                    .button("\uD83D\uDED1 Esci", "exit", null)
                    .endRow()
                    .buildEditMessage());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
