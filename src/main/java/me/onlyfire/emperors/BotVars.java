package me.onlyfire.emperors;

import java.util.Objects;

public record BotVars(String token, String username, String uri, String imgur) {

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getUri() {
        return uri;
    }

    public String getImgur() {
        return imgur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotVars botVars = (BotVars) o;
        return Objects.equals(token, botVars.token) && Objects.equals(username, botVars.username) && Objects.equals(uri, botVars.uri) && Objects.equals(imgur, botVars.imgur);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, username, uri, imgur);
    }

}
