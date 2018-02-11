package com.alivanov.intellij.plugins.liquigen;

import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiElement;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

public class CompareDatabaseAction extends LiquibaseAction {

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

        final DbDataSource targetDataSource = (DbDataSource) psiElement;
        final List<DbDataSource> referenceDataSources = collectOtherDataSourcesInProject(project, targetDataSource);
        showDataSourceSelectionPopup(e, project, targetDataSource, referenceDataSources);
    }

    private void showDataSourceSelectionPopup(AnActionEvent e, Project project, DbDataSource targetDataSource, List<DbDataSource> referenceDataSources) {
        final JList<DbDataSource> list = new JBList<>(JBList.createDefaultListModel(
                referenceDataSources.toArray(new DbDataSource[referenceDataSources.size()])));
        JBPopup popup = JBPopupFactory.getInstance()
                .createListPopupBuilder(list)
                .setTitle("Select reference Data Source")
                .setItemChoosenCallback(
                        () -> generateDiffChangeLog(project, targetDataSource, list.getSelectedValue()))
                .createPopup();
        popup.showInBestPositionFor(e.getDataContext());
    }

    private List<DbDataSource> collectOtherDataSourcesInProject(Project project, DbDataSource targetDataSource) {
        List<DbDataSource> projectDataSources = DbPsiFacade.getInstance(project).getDataSources();
        return projectDataSources.stream().filter(dataSource -> !dataSource.equals(targetDataSource)).collect(Collectors.toList());
    }

    private void generateDiffChangeLog(Project project, DbDataSource targetDataSource, DbDataSource referenceDataSource) {
        new CompareDatabaseTask(project, LIQUIGEN_BACKGROUND_TASK_NAME, targetDataSource, referenceDataSource).queue();
    }

    private static class CompareDatabaseTask extends Task.Backgroundable {

        private DbDataSource targetDataSource;
        private DbDataSource referenceDataSource;
        private String changeLog;

        CompareDatabaseTask(@Nullable Project project, @Nls @NotNull String title, DbDataSource targetDataSource, DbDataSource referenceDataSource) {
            super(project, title, true);
            this.targetDataSource = targetDataSource;
            this.referenceDataSource = referenceDataSource;
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            final LiquibaseWrapper liquibaseWrapper = new LiquibaseWrapper(getProject());
            changeLog = liquibaseWrapper.generateDiff(targetDataSource, referenceDataSource);
        }

        @Override
        public void onSuccess() {
            EditorWrapper.openInEditor(getProject(), getChangeLogName(targetDataSource.getName(), referenceDataSource.getName()), changeLog);
        }

        @Override
        public void onThrowable(@NotNull Throwable error) {
            if (error instanceof Exception) {
                Notification notification = new Notification(LIQUIGEN_GROUP_ID, LIQUIGEN_ERROR_MESSAGE_TITLE, error.getMessage(), NotificationType.ERROR);
                Notifications.Bus.notify(notification);
            }
        }
    }

    private static String getChangeLogName(String targetDataSourceName, String referenceDataSourceName) {
        return targetDataSourceName + "_" + referenceDataSourceName;
    }
}
