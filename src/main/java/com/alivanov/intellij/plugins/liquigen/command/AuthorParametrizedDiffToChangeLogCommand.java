package com.alivanov.intellij.plugins.liquigen.command;

import liquibase.command.CommandResult;
import liquibase.command.core.DiffToChangeLogCommand;
import liquibase.diff.DiffResult;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.util.StringUtils;

import java.io.PrintStream;

// Not possible to customize author in the original Liquibase class
public class AuthorParametrizedDiffToChangeLogCommand extends DiffToChangeLogCommand {

    private String author;

    public String getAuthor() {
        return author;
    }

    public AuthorParametrizedDiffToChangeLogCommand setAuthor(String author) {
        this.author = author;
        return this;
    }

    @Override
    protected CommandResult run() throws Exception {
        DiffResult diffResult = createDiffResult();

        DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult, getDiffOutputControl());

        changeLogWriter.setChangeSetAuthor(getAuthor());
        changeLogWriter.setChangeSetPath(getChangeLogFile());

        PrintStream outputStream = this.getOutputStream();
        if (outputStream == null) {
            outputStream = System.out;
        }

        if (StringUtils.trimToNull(getChangeLogFile()) == null) {
            changeLogWriter.print(outputStream);
        } else {
            changeLogWriter.print(getChangeLogFile());
        }
        return null;
    }

}
