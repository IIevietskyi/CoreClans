package dev.iievietskyi.coreclans.model;

import java.util.Locale;

public enum ObjectiveType {
    CAPTURE,
    CRYSTAL,
    BEACON;

    public static ObjectiveType fromInput(String input) {
        if (input == null) {
            return null;
        }

        String normalized = input.trim().toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        while (normalized.endsWith("_")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if ("capture".equals(normalized) || "cap".equals(normalized) || "point".equals(normalized)) {
            return CAPTURE;
        }
        if ("beacon".equals(normalized) || "beac".equals(normalized) || "mayak".equals(normalized)) {
            return BEACON;
        }
        if ("crystal".equals(normalized) || "core".equals(normalized)) {
            return CRYSTAL;
        }

        for (ObjectiveType value : values()) {
            if (value.name().equalsIgnoreCase(normalized)) {
                return value;
            }
        }
        return null;
    }
}