package me.nhan.cartbr.locale;

public enum Language {
    EN("en", "lang_en.yml"),
    VI("vi", "lang_vi.yml");

    private final String code;
    private final String fileName;

    Language(String code, String fileName) {
        this.code = code;
        this.fileName = fileName;
    }

    public String getCode() { return code; }
    public String getFileName() { return fileName; }

    public Language next() {
        return this == EN ? VI : EN;
    }

    public static Language fromCode(String code) {
        if (code == null) return EN;
        for (Language lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) return lang;
        }
        return EN;
    }
}
