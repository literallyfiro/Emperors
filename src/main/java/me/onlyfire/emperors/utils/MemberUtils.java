package me.onlyfire.emperors.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@UtilityClass
public final class MemberUtils {

    public static boolean isNormalUser(AbsSender absSender, User user, Chat chat) {
        ChatMember member = getChatMember(absSender, user, chat);

        if (member == null)
            return false;

        return !member.getStatus().equals("creator") && !member.getStatus().equals("administrator");
    }

    private static ChatMember getChatMember(AbsSender absSender, User user, Chat chat) {
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(String.valueOf(chat.getId()));
        getChatMember.setUserId(user.getId());
        try {
            return absSender.execute(getChatMember);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

}
