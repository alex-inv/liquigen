package com.alivanov.intellij.plugins.liquigen.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.alivanov.intellij.plugins.liquigen.Constants.LIQUIGEN_PLUGIN_NAME;

public class LiquigenSettings implements Configurable {

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return LIQUIGEN_PLUGIN_NAME;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return null;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }
}
