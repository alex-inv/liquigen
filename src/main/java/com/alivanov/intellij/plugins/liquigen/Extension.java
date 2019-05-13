package com.alivanov.intellij.plugins.liquigen;

public enum Extension {
    XML("xml"),
    YAML("yaml"),
    JSON("json");

    private String extension;

    Extension(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
