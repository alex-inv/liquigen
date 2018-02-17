package com.alivanov.intellij.plugins.liquigen;

import com.intellij.openapi.actionSystem.AnAction;

public abstract class LiquibaseAction extends AnAction {

    static final String LIQUIGEN_GROUP_ID = "liquigen";
    static final String LIQUIGEN_ERROR_MESSAGE_TITLE = "Error occurred during generating a changeset.";

    static final String LIQUIGEN_BACKGROUND_TASK_NAME = "Generating Change Log...";

    static final String LIQUIGEN_NO_DATA_SOURCES_FOUND = "No alternate Data Sources found in the project";
}
