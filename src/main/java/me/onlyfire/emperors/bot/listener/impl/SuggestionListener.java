package me.onlyfire.emperors.bot.listener.impl;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.listener.BotListener;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

public record SuggestionListener(EmperorsBot emperorsBot) implements BotListener {

    @Override
    public void execute(Update update, AbsSender sender) {
        //TODO Make this with mongodb
//        if (update.hasCallbackQuery()) {
//            String data = update.getCallbackQuery().getData();
//            if (data == null)
//                return;
//
//            String chatId = data.substring(data.lastIndexOf("_") + 1);
//            String userId = data.substring(data.indexOf("_") + 1);
//
//            switch (data) {
//                case "accept_suggestion" -> {
//                    for (TList lists : emperorsBot.trelloBoard.fetchLists()) {
//                        if (!lists.getName().contains("TODO"))
//                            continue;
//                        for (Suggestion suggestion : emperorsBot.suggestionsOnGoing) {
//                            if (suggestion.userId().equals(Long.valueOf(userId)) && suggestion.chatId().equals(Long.valueOf(chatId))) {
//                                Card card = new Card();
//                                card.setName(suggestion.message());
//                                card.addLabels("Proposte", "TODO");
//                                lists.createCard(card);
//                                break;
//                            }
//                        }
//
//                    }
//                }
//                case "refuse_suggestion" -> {
//                    break;
//                }
//                case "report_suggestion" -> {
//                    break;
//                }
//            }
//        }
    }
}
