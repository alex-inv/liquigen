package com.alivanov.intellij.plugins.liquigen;

import com.intellij.database.psi.DbDataSource;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

public class GenerateChangesetAction extends LiquibaseAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        final PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        if (!isDataSourceSelected(psiElement)) {
            return;
        }

        final LiquibaseWrapper liquibaseWrapper = new LiquibaseWrapper(project);

        // final DbDataSource dataSource = DbPsiFacade.getInstance(project).findDataSource((DasObject) psiElement);
        final DbDataSource dataSource = (DbDataSource) psiElement;

        String changeLog;
        try {
            changeLog = liquibaseWrapper.generateChangeLog(dataSource);
        } catch (Exception ex) {
            Notification notification = new Notification(LIQUIGEN_GROUP_ID, LIQUIGEN_ERROR_MESSAGE_TITLE, ex.getMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification);

            return;
        }

        EditorWrapper.openInEditor(project, dataSource.getName(), changeLog);
    }
}
