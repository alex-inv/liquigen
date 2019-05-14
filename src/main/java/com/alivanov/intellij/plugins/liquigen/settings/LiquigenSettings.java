package com.alivanov.intellij.plugins.liquigen.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.alivanov.intellij.plugins.liquigen.Constants.LIQUIGEN_PLUGIN_NAME;

public class LiquigenSettings implements SearchableConfigurable {

    LiquigenSettingsUI settingsUI;
    LiquigenConfig settingsConfig;

    public LiquigenSettings(Project project) {
        settingsConfig = LiquigenConfig.getInstance(project);
    }

    @NotNull
    @Override
    public String getId() {
        return "com.alivanov.intellij.plugins.liquigen";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return LIQUIGEN_PLUGIN_NAME;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsUI = new LiquigenSettingsUI(settingsConfig);
        return settingsUI.getSettingsPanel();
    }

    @Override
    public boolean isModified() {
        return settingsUI.isModified();
    }

    @Override
    public void apply() {
        settingsUI.apply();
    }

    @Override
    public void reset() {
        settingsUI.reset();
    }

    @Override
    public void disposeUIResources() {
        settingsUI = null;
    }
}
