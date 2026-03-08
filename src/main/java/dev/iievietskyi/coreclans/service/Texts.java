package dev.iievietskyi.coreclans.service;

import java.util.Collections;
import java.util.Map;

import org.bukkit.ChatColor;

public final class Texts {
    private Texts() {
    }

    public static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }

    public static String replace(String input, Map<String, String> replacements) {
        String out = input == null ? "" : input;
        Map<String, String> map = replacements == null ? Collections.<String, String>emptyMap() : replacements;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            out = out.replace("{" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
        }
        return out;
    }

    public static String format(String input, Map<String, String> replacements) {
        return color(replace(input, replacements));
    }
}
