package com.alivanov.intellij.plugins.liquigen.settings;

import com.alivanov.intellij.plugins.liquigen.Extension;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "com.alivanov.intellij.plugins.liquigen.config")
public class LiquigenConfig implements PersistentStateComponent<LiquigenConfig> {

    private String author;
    private Extension extension;

    public LiquigenConfig() {
        this.extension = Extension.XML;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public Extension getExtension() {
        return extension;
    }

    public void setExtension(final Extension extension) {
        this.extension = extension;
    }

    @Nullable
    @Override
    public LiquigenConfig getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull final LiquigenConfig config) {
        XmlSerializerUtil.copyBean(config, this);
    }

    public static LiquigenConfig getInstance(Project project) {
        return ServiceManager.getService(project, LiquigenConfig.class);
    }
}
