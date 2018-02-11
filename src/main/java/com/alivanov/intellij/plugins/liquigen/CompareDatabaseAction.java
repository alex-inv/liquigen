package com.alivanov.intellij.plugins.liquigen;

import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiElement;
import com.intellij.ui.components.JBList;

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

        final JList<DbDataSource> list = new JBList<>(JBList.createDefaultListModel(
                referenceDataSources.toArray(new DbDataSource[referenceDataSources.size()])));
        JBPopup popup = JBPopupFactory.getInstance()
                .createListPopupBuilder(list)
                .setTitle("Select reference Data Source")
                .setItemChoosenCallback(dataSourceSelectedCallback(project, targetDataSource, list))
                .createPopup();
        popup.showInBestPositionFor(e.getDataContext());
    }

    private List<DbDataSource> collectOtherDataSourcesInProject(Project project, DbDataSource targetDataSource) {
        List<DbDataSource> projectDataSources = DbPsiFacade.getInstance(project).getDataSources();
        return projectDataSources.stream().filter(dataSource -> !dataSource.equals(targetDataSource)).collect(Collectors.toList());
    }

    private Runnable dataSourceSelectedCallback(Project project, DbDataSource targetDataSource, JList<DbDataSource> list) {
        return () -> generateDiffChangeLog(project, targetDataSource, list.getSelectedValue());
    }

    private void generateDiffChangeLog(Project project, DbDataSource targetDataSource, DbDataSource referenceDataSource) {
        final LiquibaseWrapper liquibaseWrapper = new LiquibaseWrapper(project);

        String changeLog;
        try {
            changeLog = liquibaseWrapper.generateDiff(targetDataSource, referenceDataSource);
        } catch (Exception ex) {
            Notification notification = new Notification(LIQUIGEN_GROUP_ID, LIQUIGEN_ERROR_MESSAGE_TITLE, ex.getMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification);

            return;
        }

        EditorWrapper.openInEditor(project, getChangeLogName(targetDataSource.getName(), referenceDataSource.getName()), changeLog);
    }

    private String getChangeLogName(String targetDataSourceName, String referenceDataSourceName) {
        return targetDataSourceName + "_" + referenceDataSourceName;
    }
}
