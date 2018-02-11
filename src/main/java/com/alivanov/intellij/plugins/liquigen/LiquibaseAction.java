package com.alivanov.intellij.plugins.liquigen;

import com.intellij.database.psi.DbDataSource;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

public abstract class LiquibaseAction extends AnAction {

    static final String LIQUIGEN_GROUP_ID = "liquigen";
    static final String LIQUIGEN_ERROR_MESSAGE_TITLE = "Error occurred during generating a changeset.";

    static final String LIQUIGEN_BACKGROUND_TASK_NAME = "Generating change log...";

    @Override
    public void update(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        final PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        if (psiElement == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        e.getPresentation().setEnabledAndVisible(isDataSourceSelected(psiElement));
        super.update(e);
    }

    boolean isDataSourceSelected(PsiElement element) {
        return element instanceof DbDataSource;
    }

}
