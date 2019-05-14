package com.alivanov.intellij.plugins.liquigen;

public enum Extension {
    XML("xml", "XML"),
    YAML("yaml", "yaml"),
    JSON("json", "JSON");

    private String extension;
    private String languageId;

    Extension(String extension, String languageId) {
        this.extension = extension;
        this.languageId = languageId;
    }

    public String getExtension() {
        return extension;
    }

    public String getLanguageId() {
        return languageId;
    }
}
