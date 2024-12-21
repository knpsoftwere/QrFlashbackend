package org.qrflash.Enum;

public enum ItemType {
    WARH("warh"), TECH("tech"), MOD("mod"), SERV("serv");

    private final String value;

    ItemType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValid(String value) {
        for (ItemType itemType : ItemType.values()) {
            if (itemType.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
