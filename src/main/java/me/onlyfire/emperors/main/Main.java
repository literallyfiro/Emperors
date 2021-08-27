/*
 * Copyright (c) 2021.
 * The Emperors project is controlled by the GNU General Public License v3.0.
 * You can find it in the LICENSE file on the GitHub repository.
 */

package me.onlyfire.emperors.main;

import me.onlyfire.emperors.Bot;

import java.util.Locale;

public class Main {

    public static void main(String[] args) {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (!OS.contains("nux")) {
            System.out.println("Run this only from linux, thank you");
            return;
        }
        new Bot();
    }


}
