package com.alivanov.intellij.plugins.liquigen;

import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.alivanov.intellij.plugins.liquigen.Constants.*;

public class CompareDatabaseAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        final PsiElement[] psiElementArray = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (psiElementArray == null || psiElementArray.length == 0) {
            return;
        }

        final DbDataSource targetDataSource = (DbDataSource) psiElementArray[0];

        if (isSingleDataSourceSelected(psiElementArray)) {
            final List<DbDataSource> referenceDataSources = collectOtherDataSourcesInProject(project, targetDataSource);
            showDataSourceSelectionPopup(e, project, targetDataSource, referenceDataSources);
        } else if (areTwoDataSourcesSelected(psiElementArray)) {
            final DbDataSource referenceDataSource = (DbDataSource) psiElementArray[1];
            generateDiffChangeLog(project, targetDataSource, referenceDataSource);
        }
    }

    private void showDataSourceSelectionPopup(AnActionEvent e, Project project, DbDataSource targetDataSource, List<DbDataSource> referenceDataSources) {
        JBPopup jbPopup = JBPopupFactory.getInstance()
                .createPopupChooserBuilder(referenceDataSources)
                .setTitle("Select Reference Data Source")
                .setRenderer(new DataSourceCellRenderer())
                .setItemChosenCallback(value -> generateDiffChangeLog(project, targetDataSource, value))
                .createPopup();

        jbPopup.showInBestPositionFor(e.getDataContext());
    }

    private List<DbDataSource> collectOtherDataSourcesInProject(Project project, DbDataSource targetDataSource) {
        List<DbDataSource> projectDataSources = DbPsiFacade.getInstance(project).getDataSources();
        return projectDataSources.stream().filter(dataSource -> !dataSource.equals(targetDataSource)).collect(Collectors.toList());
    }

    private void generateDiffChangeLog(Project project, DbDataSource targetDataSource, DbDataSource referenceDataSource) {
        if (referenceDataSource != null) {
            new CompareDatabaseTask(project, LIQUIGEN_BACKGROUND_TASK_NAME, targetDataSource, referenceDataSource).queue();
        }
    }

    private static class CompareDatabaseTask extends Task.Backgroundable {

        private final DbDataSource targetDataSource;
        private final DbDataSource referenceDataSource;
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

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        final PsiElement[] psiElementArray = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (psiElementArray == null || psiElementArray.length == 0) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        e.getPresentation().setEnabledAndVisible(isSingleDataSourceSelected(psiElementArray) || areTwoDataSourcesSelected(psiElementArray));
        super.update(e);
    }

    private boolean isSingleDataSourceSelected(PsiElement[] psiElementArray) {
        return psiElementArray.length == 1 && psiElementArray[0] instanceof DbDataSource;
    }

    private boolean areTwoDataSourcesSelected(PsiElement[] psiElementArray) {
        return psiElementArray.length == 2 && psiElementArray[0] instanceof DbDataSource && psiElementArray[1] instanceof DbDataSource;
    }

    private static class DataSourceCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            DbDataSource dataSource = (DbDataSource) value;

            setIcon(dataSource.getIcon());
            setText(dataSource.getName());

            return this;
        }
    }
}
