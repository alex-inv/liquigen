package com.alivanov.intellij.plugins.liquigen;

import com.intellij.database.psi.DbDataSource;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        final DbDataSource dataSource = (DbDataSource) psiElement;
        new GenerateChangeLogTask(project, LIQUIGEN_BACKGROUND_TASK_NAME, dataSource).queue();
    }

    private static class GenerateChangeLogTask extends Task.Backgroundable {

        private DbDataSource dataSource;
        private String changeLog;

        GenerateChangeLogTask(@Nullable Project project, @Nls @NotNull String title, DbDataSource dataSource) {
            super(project, title, true);
            this.dataSource = dataSource;
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            final LiquibaseWrapper liquibaseWrapper = new LiquibaseWrapper(getProject());
            changeLog = liquibaseWrapper.generateChangeLog(dataSource);
        }

        @Override
        public void onSuccess() {
            EditorWrapper.openInEditor(getProject(), dataSource.getName(), changeLog);
        }

        @Override
        public void onThrowable(@NotNull Throwable error) {
            if (error instanceof Exception) {
                Notification notification = new Notification(LIQUIGEN_GROUP_ID, LIQUIGEN_ERROR_MESSAGE_TITLE, error.getMessage(), NotificationType.ERROR);
                Notifications.Bus.notify(notification);
            }
        }
    }
}
