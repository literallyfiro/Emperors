package me.onlyfire.emperors.bot.commands;

public class SuggestionCommand {

//    public SuggestionCommand() {
//        super("suggestion", "Suggerisci una nuova funzione per il bot");
//    }
//
//    @Override
//    public void execute(AbsSender absSender, User user, Message message, Chat chat, String[] strings) {
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.enableHtml(true);
//        sendMessage.setChatId(String.valueOf(chat.getId()));
//        sendMessage.setReplyToMessageId(message.getMessageId());
//
//        if (!message.hasText())
//            return;
//
//        if (CooldownManager.getInstance().isInCooldown(user, chat)) {
//            sendMessage.setText(user.getFirstName() + " Hai già inviato una richiesta precedentemente! Aspetta un po' per inviarne un'altra");
//            try {
//                absSender.executeAsync(sendMessage);
//            } catch (TelegramApiException e) {
//                e.printStackTrace();
//            }
//            return;
//        }
//
//        sendRequest(absSender, user, message, chat);
//        sendMessage.setText("La tua richiesta è stata inoltrata correttamente!");
//        try {
//            absSender.executeAsync(sendMessage);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//
//        CooldownManager.getInstance().createCooldown(user, chat, 5, TimeUnit.MINUTES);
//    }
//
//    private void sendRequest(AbsSender absSender, User user, Message message, Chat chat) {
//        String[] strings = message.getText().split(" ");
//        String text = String.join(" ", strings).replace(strings[0], "");
//
//        SendMessage sendMessage = InlineKeyboardBuilder.create(-1001504716707L)
//                .setText("<b>Nuova suggestion!</b>\n" +
//                        "🐤 <b>Utente:</b> <code>" + user.getFirstName() + (user.getUserName() != null ? user.getUserName() : "") + "</code>\n" +
//                        "📄 <b>Messaggio:</b> <code>" + text + "</code>\n" +
//                        "🗯 <b>Chat ID:</b> <code>" + chat.getId() + "</code>")
//                .row()
//                .button("✅ ACCETTA", "accept_suggestion", null)
//                .button("❌ RIFIUTA", "refuse_suggestion", null)
//                .endRow()
//                .row()
//                .button("⚠️ REPORT", "report_suggestion", null)
//                .endRow()
//                .build();
//
//        try {
//            absSender.executeAsync(sendMessage);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//
//        //TODO, Add to mongodb instead of java lists
////        emperorsBot.suggestionsOnGoing.add(new Suggestion(chat.getId(), text, user.getId()));
//    }
}
