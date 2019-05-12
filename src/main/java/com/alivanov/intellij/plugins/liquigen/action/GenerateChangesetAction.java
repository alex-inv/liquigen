package com.alivanov.intellij.plugins.liquigen.action;

import com.alivanov.intellij.plugins.liquigen.EditorWrapper;
import com.alivanov.intellij.plugins.liquigen.LiquibaseWrapper;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbElement;
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
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.alivanov.intellij.plugins.liquigen.Constants.*;

public class GenerateChangesetAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        final PsiElement[] psiElementArray = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (psiElementArray == null || psiElementArray.length == 0) {
            return;
        }

        List<DbElement> dbElements = Arrays.stream(psiElementArray).map(element -> (DbElement) element).collect(Collectors.toList());

        // We can generate a changeset only for a single data source. Check if all elements belong to first element's data source
        final DbDataSource dataSource = DbPsiFacade.getInstance(project).findDataSource(dbElements.get(0));
        if (dataSource == null) {
            return;
        }

        if (!areElementsOfOneDataSource(project, dbElements, dataSource)) {
            Notification notification = new Notification(LIQUIGEN_GROUP_ID, LIQUIGEN_ERROR_MESSAGE_TITLE
                    ,LIQUIGEN_ERROR_MESSAGE_TOO_MANY_DATA_SOURCES, NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            return;
        }

        new GenerateChangeLogTask(project, dbElements, dataSource).queue();
    }

    private boolean areElementsOfOneDataSource(Project project, List<DbElement> dbElements, DbDataSource dataSource) {
        return dbElements.stream().map(dbElement -> DbPsiFacade.getInstance(project).findDataSource(dbElement)).filter(Objects::nonNull)
                .allMatch(dbDatSource -> dbDatSource.equals(dataSource));
    }

    private static class GenerateChangeLogTask extends Task.Backgroundable {

        private final List<DbElement> dbElements;
        private final DbDataSource dataSource;
        private String changeLog;

        GenerateChangeLogTask(@Nullable Project project, List<DbElement> dbElements, DbDataSource dataSource) {
            super(project, LIQUIGEN_BACKGROUND_TASK_NAME, true);
            this.dbElements = dbElements;
            this.dataSource = dataSource;
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            final LiquibaseWrapper liquibaseWrapper = new LiquibaseWrapper(getProject());
            changeLog = liquibaseWrapper.generateChangeLog(dbElements, dataSource);
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

    @Override
    public void update(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        final PsiElement[] psiElementArray = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (psiElementArray == null || psiElementArray.length == 0) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        e.getPresentation().setEnabledAndVisible(areAllDatabaseElementsSelected(psiElementArray));
        super.update(e);
    }

    private boolean areAllDatabaseElementsSelected(PsiElement[] psiElementArray) {
        return Arrays.stream(psiElementArray).allMatch(element -> element instanceof DbElement);
    }
}
