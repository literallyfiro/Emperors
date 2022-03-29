package me.onlyfire.emperors.utils;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

@UtilityClass
public class Downloader {

    public static void download(org.telegram.telegrambots.meta.api.objects.File photoFile, File finalFile, String token) {
        try {
            URL url = new URL(photoFile.getFileUrl(token));
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream output = new FileOutputStream(finalFile);
            output.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}