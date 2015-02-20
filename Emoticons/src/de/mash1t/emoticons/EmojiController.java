/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mash1t.emoticons;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import sun.misc.Launcher;

/**
 *
 * @author Manuel Schmid
 */
public class EmojiController {

    private static final Map<String, String> emojiMap = new HashMap<>();

    public EmojiController() throws IOException {

        final String path = "de/mash1t/emoticons";
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath().replace("%20", " "));

        if (jarFile.isFile()) {  // Run with JAR file
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            while (entries.hasMoreElements()) {
                final String name = entries.nextElement().getName();
                if (name.startsWith(path + "/") && name.endsWith(".png")) { //filter according to the path
                    emojiMap.put(name.replace(".png", ""), name);
                }
            }
            jar.close();
        } else { // Run with IDE
            final URL url = Launcher.class.getResource("/" + path);
            if (url != null) {
                try {
                    final File apps = new File(url.toURI());
                    for (File app : apps.listFiles()) {
                        if (app.getName().endsWith(".png")) { //filter according to the path
                            emojiMap.put(app.getName().replace(".png", ""), app.getName());
                        }
                    }
                } catch (URISyntaxException ex) {
                    // Never happens
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        EmojiController cont = new EmojiController();
        String aaa = emojiMap.get("abcd");
    }
}
