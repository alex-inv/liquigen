package com.alivanov.intellij.plugins.liquigen.command;

import liquibase.command.core.DiffToChangeLogCommand;
import liquibase.diff.DiffResult;
import liquibase.diff.output.changelog.DiffToChangeLog;

public class AuthorParametrizedDiffToChangeLogCommand extends DiffToChangeLogCommand {

    private String author;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    // Not possible to customize author in the original Liquibase class
    @Override
    protected DiffToChangeLog createDiffToChangeLogObject(DiffResult diffResult) {
        DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, getDiffOutputControl());
        diffToChangeLog.setChangeSetAuthor(getAuthor());
        return diffToChangeLog;
    }

}
