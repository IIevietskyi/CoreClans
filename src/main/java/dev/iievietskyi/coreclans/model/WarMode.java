package dev.iievietskyi.coreclans.model;

public enum WarMode {
    CAPTURE,
    BEACON,
    CRYSTAL;

    public static WarMode fromInput(String input) {
        if (input == null) {
            return null;
        }
        for (WarMode value : values()) {
            if (value.name().equalsIgnoreCase(input)) {
                return value;
            }
        }
        return null;
    }

    public ObjectiveType requiredObjective() {
        if (this == CAPTURE) {
            return ObjectiveType.CAPTURE;
        }
        if (this == BEACON) {
            return ObjectiveType.BEACON;
        }
        return ObjectiveType.CRYSTAL;
    }
}
