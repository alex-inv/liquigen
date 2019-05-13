package com.alivanov.intellij.plugins.liquigen.wrapper;

import com.alivanov.intellij.plugins.liquigen.Extension;
import com.alivanov.intellij.plugins.liquigen.liquibase.LiquigenDiffToChangeLogCommand;
import com.alivanov.intellij.plugins.liquigen.liquibase.LiquigenGenerateChangelogCommand;
import com.intellij.database.dataSource.DatabaseConnection;
import com.intellij.database.dataSource.DatabaseConnectionManager;
import com.intellij.database.model.DasObject;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbElement;
import com.intellij.database.psi.DbNamespaceImpl;
import com.intellij.database.util.DbImplUtil;
import com.intellij.database.util.GuardedRef;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.Project;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.exception.DatabaseException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.alivanov.intellij.plugins.liquigen.Constants.LIQUIGEN_BACKGROUND_TASK_NAME;

public class LiquibaseWrapper {

    private final Project project;

    public LiquibaseWrapper(Project project) {
        this.project = project;
    }

    public String generateChangeLog(List<DbElement> dbElements, DbDataSource dataSource) {
        DiffOutputControl diffOutputControl = createDiffOutputControlForElements(dbElements);

        String generatedChangeLog;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(byteStream, true, "utf-8");
             GuardedRef<DatabaseConnection> connectionRef = acquireConnection(dataSource)
        ) {
            startIndicatorProgress();

            Database referenceDatabase = getDatabase(connectionRef);

            LiquigenGenerateChangelogCommand command = new LiquigenGenerateChangelogCommand();
            command.setReferenceDatabase(referenceDatabase)
                    .setOutputStream(printStream)
                    .setCompareControl(createDefaultCompareControl());
            command.setDiffOutputControl(diffOutputControl);
            command.setAuthor(System.getProperty("user.name"));
            command.setExtension(Extension.XML);

            command.execute();

            generatedChangeLog = new String(byteStream.toByteArray(), StandardCharsets.UTF_8);

            finishIndicatorProgress();
        } catch (ProcessCanceledException processCanceledEx) {
            throw processCanceledEx;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return generatedChangeLog;
    }

    private DiffOutputControl createDiffOutputControlForElements(List<DbElement> dbElements) {
        Set<String> dbElementNames = dbElements.stream().filter(dbElement -> !(dbElement instanceof DbDataSource || dbElement instanceof DbNamespaceImpl))
                .map(DasObject::getName).collect(Collectors.toSet());

        if (dbElementNames.size() == 0) {
            // Only data sources or databases are selected
            return createDefaultDiffOutputControl();
        } else {
            return createFilteredDiffOutputControl(dbElementNames);
        }
    }

    public String generateDiff(DbDataSource target, DbDataSource reference) {
        String diffChangeLog;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(byteStream, true, "utf-8");
             GuardedRef<DatabaseConnection> targetConnectionRef = acquireConnection(target);
             GuardedRef<DatabaseConnection> referenceConnectionRef = acquireConnection(reference)
        ) {
            startIndicatorProgress();

            Database targetDatabase = getDatabase(targetConnectionRef);
            Database referenceDatabase = getDatabase(referenceConnectionRef);

            LiquigenDiffToChangeLogCommand command = new LiquigenDiffToChangeLogCommand();
            command.setReferenceDatabase(referenceDatabase)
                    .setTargetDatabase(targetDatabase)
                    .setOutputStream(printStream)
                    .setCompareControl(createDefaultCompareControl());
            command.setDiffOutputControl(createDefaultDiffOutputControl());
            command.setAuthor(System.getProperty("user.name"));
            command.setExtension(Extension.XML);

            command.execute();

            diffChangeLog = new String(byteStream.toByteArray(), StandardCharsets.UTF_8);

            finishIndicatorProgress();
        } catch (ProcessCanceledException processCanceledEx) {
            throw processCanceledEx;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return diffChangeLog;
    }

    private GuardedRef<DatabaseConnection> acquireConnection(DbDataSource dataSource) throws SQLException {
        return DatabaseConnectionManager.getInstance()
                .build(this.project, Objects.requireNonNull(DbImplUtil.getMaybeLocalDataSource(dataSource))).create();
    }

    private Database getDatabase(GuardedRef<DatabaseConnection> connectionRef) throws DatabaseException {
        Connection targetConn = connectionRef.get().getJdbcConnection();
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(targetConn));
    }

    // Compare default catalogs and schemas, and compare only default database objects - no data included
    private CompareControl createDefaultCompareControl() {
        return new CompareControl();
    }

    private DiffOutputControl createFilteredDiffOutputControl(Set<String> dbElementNames) {
        DiffOutputControl diffOutputControl = createDefaultDiffOutputControl();

        String elementFilter = String.join(", ", dbElementNames);
        diffOutputControl.setObjectChangeFilter(
                new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, elementFilter));

        return diffOutputControl;
    }

    // Do not include information about schema, catalog or tablespace to result XML changelog
    private DiffOutputControl createDefaultDiffOutputControl() {
        DiffOutputControl diffOutputControl = new DiffOutputControl();

        diffOutputControl.setIncludeSchema(false);
        diffOutputControl.setIncludeCatalog(false);
        diffOutputControl.setIncludeTablespace(false);

        return diffOutputControl;
    }

    private void startIndicatorProgress() {
        ProgressIndicator indicator = ProgressIndicatorProvider.getGlobalProgressIndicator();

        if (indicator != null) {
            indicator.setText(LIQUIGEN_BACKGROUND_TASK_NAME);
            indicator.setIndeterminate(true);
        }
    }

    private void finishIndicatorProgress() {
        ProgressIndicator indicator = ProgressIndicatorProvider.getGlobalProgressIndicator();

        if (indicator != null) {
            indicator.setIndeterminate(false);
            indicator.setFraction(1.0);
        }
    }
}
