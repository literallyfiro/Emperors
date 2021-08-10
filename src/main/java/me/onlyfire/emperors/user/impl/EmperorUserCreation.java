package me.onlyfire.emperors.user.impl;

import me.onlyfire.emperors.bot.EmperorsBot;
import me.onlyfire.emperors.bot.exceptions.EmperorException;
import me.onlyfire.emperors.essential.Database;
import me.onlyfire.emperors.essential.Language;
import me.onlyfire.emperors.model.Emperor;
import me.onlyfire.emperors.user.EmperorUserMode;
import me.onlyfire.emperors.utils.Downloader;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class EmperorUserCreation extends EmperorUserMode {

    private final EmperorsBot emperorsBot;
    private final AbsSender sender;
    private final User user;
    private final Chat chat;
    private final Instant instThen;

    private final String PHOTO_TEMPLATE = "cache" + File.separator + "%s.jpg";

    public EmperorUserCreation(EmperorsBot emperorsBot, AbsSender sender, User user, Chat chat, Message message) {
        super(emperorsBot, sender, user, chat, message);
        this.emperorsBot = emperorsBot;
        this.sender = sender;
        this.user = user;
        this.chat = chat;
        this.instThen = Instant.now();
    }

    @Override
    public void runCheck() {
        Instant now = Instant.now();
        Duration duration = Duration.between(instThen, now);
        long seconds = duration.getSeconds();
        long limit = TimeUnit.MINUTES.toSeconds(2);

        if (seconds > limit) {
            emperorsBot.userMode.remove(user);
            SendMessage sendMessage = new SendMessage();
            sendMessage.enableHtml(true);
            sendMessage.setChatId(String.valueOf(chat.getId()));
            sendMessage.setText("<a href=\"tg://user?id=" + user.getId() + "\">" + user.getFirstName() + "</a> " +
                    "Sei stato rimosso dalla modalità creazione per non aver risposto al messaggio in 2 minuti");
            try {
                sender.executeAsync(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            stop();
        }
    }

    public void completed(Message updatedMessage, String newEmperorName) {
        try {
            GetFile getPhoto = new GetFile();
            getPhoto.setFileId(updatedMessage.getDocument().getFileId());
            org.telegram.telegrambots.meta.api.objects.File photoFile = sender.execute(getPhoto);
            File photo = new File(String.format(PHOTO_TEMPLATE, newEmperorName));

            Downloader.download(photoFile, photo, emperorsBot.botVars.token());

            uploadToImgur(photo, (result, throwable) -> {
                if (throwable != null) {
                    emperorsBot.removeUserMode(user, chat, new EmperorException("Errore durante l'invio a Imgur (API Offline?)", throwable));
                    return;
                }

                SendMessage sendMessage = new SendMessage();
                sendMessage.enableHtml(true);
                sendMessage.setChatId(String.valueOf(chat.getId()));

                JSONObject upload = new JSONObject(result);
                JSONObject data = upload.getJSONObject("data");
                String photoId = data.getString("id");
                sendMessage.setText(String.format(Language.ADDED_EMPEROR_SUCCESSFULLY.toString(), newEmperorName));

                emperorsBot.removeUserMode(user, chat, null);

                CompletableFuture<Integer> future = Database.executeUpdate("INSERT INTO emperors VALUES (?,?,?,?,?,?);",
                        new Object[]{chat.getId(), newEmperorName, photoId, null, null, 0L});
                long before = System.currentTimeMillis();
                future.whenComplete(((integer, futureThrowable) -> {
                    if (futureThrowable != null) {
                        emperorsBot.removeUserMode(user, chat, new EmperorException("Errore durante l'inserimento del record", futureThrowable));
                        return;
                    }
                    emperorsBot.loadedEmperors.add(new Emperor(String.valueOf(chat.getId()), newEmperorName, photoId));

                    long after = (System.currentTimeMillis() - before);
                    String joining = "Created emperor %s on group %s (Familiar name: %s). Took %sms for completion.";
                    emperorsBot.LOGGER.info(String.format(joining, newEmperorName, chat.getId(), chat.getTitle(), after));

                    try {
                        sender.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }));
            });
        } catch (TelegramApiException | IOException | IllegalArgumentException e) {
            emperorsBot.removeUserMode(user, chat, new EmperorException("Errore generale di creazione", e));
        }
    }

    private void uploadToImgur(File file, UploadCallback callback) throws IOException {
        String data = getImageData(file);
        if (data.isEmpty()) return;

        var client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.imgur.com/3/image"))
                .timeout(Duration.ofSeconds(15))
                .headers(
                        "Content-Type", "application/x-www-form-urlencoded",
                        "Authorization", "Client-ID " + emperorsBot.botVars.imgur()
                )
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .whenComplete((callback::onUpload));
    }

    private String getImageData(File file) throws IOException {
        if (file == null) {
            emperorsBot.removeUserMode(user, chat, new EmperorException("Errore durante la codifica del file"));
            return "";
        }
        BufferedImage image = ImageIO.read(file);
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArray);
        byte[] byteImage = byteArray.toByteArray();

        String dataImage = Base64.getEncoder().encodeToString(byteImage);
        return URLEncoder.encode("image", StandardCharsets.UTF_8) + "="
                + URLEncoder.encode(dataImage, StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    private interface UploadCallback {
        void onUpload(String result, Throwable throwable);
    }

}
