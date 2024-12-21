package org.qrflash.Enum;

public enum unit {
    SHT("sht"), KG("kg"), G("g"), L("l"), ML("ml"), PRC("prc");

    private final String value;

    unit(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValid(String value) {
        for (org.qrflash.Enum.unit unit : unit.values()) {
            if (unit.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
