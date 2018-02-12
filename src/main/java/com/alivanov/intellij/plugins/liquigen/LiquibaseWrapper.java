package com.alivanov.intellij.plugins.liquigen;

import com.intellij.database.dataSource.DatabaseConnection;
import com.intellij.database.dataSource.DatabaseConnectionManager;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbElement;
import com.intellij.database.util.GuardedRef;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import liquibase.command.DiffToChangeLogCommand;
import liquibase.command.GenerateChangeLogCommand;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.StandardObjectChangeFilter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

class LiquibaseWrapper {

    private Project project;

    LiquibaseWrapper(Project project) {
        this.project = project;
    }

    public String generateChangeLog(DbElement dbElement, DbDataSource dataSource) {
        return doGenerateChangeLog(dataSource, createFilteredDiffOutputControl(dbElement));
    }

    public String generateChangeLog(DbDataSource dataSource) {
        return doGenerateChangeLog(dataSource, getDefaultDiffOutputControl());
    }

    private String doGenerateChangeLog(DbDataSource dataSource, DiffOutputControl diffOutputControl) {
        String generatedChangeLog;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(byteStream, true, "utf-8");
             GuardedRef<DatabaseConnection> connectionRef = acquireConnection(dataSource)
        ) {
            Connection conn = connectionRef.get().getJdbcConnection();
            Database referenceDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));

            GenerateChangeLogCommand command = new GenerateChangeLogCommand();

            command.setReferenceDatabase(referenceDatabase)
                    .setOutputStream(printStream)
                    .setCompareControl(getDefaultCompareControl());
            command.setDiffOutputControl(diffOutputControl);

            command.execute();

            generatedChangeLog = new String(byteStream.toByteArray(), StandardCharsets.UTF_8);
        } catch (ProcessCanceledException processCanceledEx) {
            throw processCanceledEx;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return generatedChangeLog;
    }

    public String generateDiff(DbDataSource target, DbDataSource reference) {
        String diffChangeLog;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(byteStream, true, "utf-8");
             GuardedRef<DatabaseConnection> targetConnectionRef = acquireConnection(target);
             GuardedRef<DatabaseConnection> referenceConnectionRef = acquireConnection(reference)
        ) {
            Connection targetConn = targetConnectionRef.get().getJdbcConnection();
            Database targetDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(targetConn));

            Connection referenceConn = referenceConnectionRef.get().getJdbcConnection();
            Database referenceDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(referenceConn));

            DiffToChangeLogCommand command = new DiffToChangeLogCommand();
            command.setReferenceDatabase(referenceDatabase)
                    .setTargetDatabase(targetDatabase)
                    .setOutputStream(printStream)
                    .setCompareControl(getDefaultCompareControl());
            command.setDiffOutputControl(getDefaultDiffOutputControl());

            command.execute();

            diffChangeLog = new String(byteStream.toByteArray(), StandardCharsets.UTF_8);
        } catch (ProcessCanceledException processCanceledEx) {
            throw processCanceledEx;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return diffChangeLog;
    }

    private GuardedRef<DatabaseConnection> acquireConnection(DbDataSource dataSource) throws SQLException {
        return DatabaseConnectionManager.getInstance()
                .build(this.project, (LocalDataSource) dataSource.getDelegate()).create();
    }

    // Compare default catalogs and schemas, and compare only default database objects - no data included
    private CompareControl getDefaultCompareControl() {
        return new CompareControl();
    }

    private DiffOutputControl createFilteredDiffOutputControl(DbElement dbElement) {
        DiffOutputControl diffOutputControl = new DiffOutputControl();

        diffOutputControl.setIncludeSchema(false);
        diffOutputControl.setIncludeCatalog(false);
        diffOutputControl.setIncludeTablespace(false);

        diffOutputControl.setObjectChangeFilter(
                new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, dbElement.getName()));

        return diffOutputControl;
    }

    private DiffOutputControl getDefaultDiffOutputControl() {
        DiffOutputControl diffOutputControl = new DiffOutputControl();

        diffOutputControl.setIncludeSchema(false);
        diffOutputControl.setIncludeCatalog(false);
        diffOutputControl.setIncludeTablespace(false);

        return diffOutputControl;
    }
}
