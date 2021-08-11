package me.onlyfire.emperors.bot.listener.impl;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.listener.BotListener;
import me.onlyfire.emperors.essential.StopAction;
import me.onlyfire.emperors.utils.InlineKeyboardBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public record AdminPanelListener(EmperorsBot emperorsBot) implements BotListener {

    @Override
    public void execute(Update update, AbsSender sender) {
        if (!update.hasCallbackQuery()) return;
        String data = update.getCallbackQuery().getData();
        if (data == null) return;

        switch (data) {
            case "new_update_bot" -> {
                break;
            }
            case "stop_bot" -> {
                SendMessage sendMessage = InlineKeyboardBuilder.create(update.getCallbackQuery().getMessage().getChatId())
                        .setText("Sei sicuro di voler stoppare il bot? Non sarà più possibile riavviarlo se non accedendo alla macchina che ospita il bot!")
                        .row()
                        .button("Sono sicuro, stoppa il bot ✔️", "stop_bot_sure", null)
                        .endRow()
                        .row()
                        .button("NO! Non stoppare il bot ❌", "stop_bot_cancel", null)
                        .endRow()
                        .build();
                try {
                    sender.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            case "stop_bot_sure" -> {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
                editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                editMessageText.setText("☑️ Inviata richiesta di stop al server. entro 3 secondi il bot si interromperà...");
                try {
                    sender.execute(editMessageText);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                emperorsBot.handleStopAction(update.getCallbackQuery().getFrom(), StopAction.STOP);
            }
            case "stop_bot_cancel", "restart_bot_cancel" -> {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                deleteMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
                try {
                    sender.executeAsync(deleteMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            case "restart_bot" -> {
                SendMessage sendMessage = InlineKeyboardBuilder.create(update.getCallbackQuery().getMessage().getChatId())
                        .setText("Sei sicuro di voler riavviare il bot?")
                        .row()
                        .button("Sono sicuro, riavvia il bot ✔️", "restart_bot_sure", null)
                        .endRow()
                        .row()
                        .button("NO! Non riavviare il bot ❌", "restart_bot_cancel", null)
                        .endRow()
                        .build();
                try {
                    sender.executeAsync(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            case "restart_bot_sure" -> {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
                editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                editMessageText.setText("☑️ Inviata richiesta di riavvio al server. entro 3 secondi il bot si riavvierà...");
                try {
                    sender.executeAsync(editMessageText);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                emperorsBot.handleStopAction(update.getCallbackQuery().getFrom(), StopAction.RESTART);
            }
        }
    }

}
