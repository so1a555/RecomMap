package jp.ac.jec.cm0128.recommap;

public enum ItemCategory {
    RAMEN("ラーメン"),
    CONVENI("コンビニ"),
    JEC("日本電子 建物");

    private final String displayName;

    ItemCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
