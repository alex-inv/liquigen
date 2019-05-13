package com.alivanov.intellij.plugins.liquigen.liquibase;

import com.alivanov.intellij.plugins.liquigen.Extension;
import liquibase.command.CommandResult;
import liquibase.command.core.DiffToChangeLogCommand;
import liquibase.diff.DiffResult;

import java.io.PrintStream;

public class LiquigenDiffToChangeLogCommand extends DiffToChangeLogCommand {

    private String author;
    private Extension extension;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Extension getExtension() {
        return extension;
    }

    public void setExtension(final Extension extension) {
        this.extension = extension;
    }

    @Override
    protected CommandResult run() throws Exception {
        DiffResult diffResult = createDiffResult();

        LiquigenDiffToChangeLog changeLogWriter = new LiquigenDiffToChangeLog(diffResult, getDiffOutputControl());
        changeLogWriter.setChangeSetAuthor(getAuthor());

        PrintStream outputStream = this.getOutputStream();
        if (outputStream == null) {
            outputStream = System.out;
        }

        changeLogWriter.print(outputStream, getExtension());

        return new CommandResult("OK");
    }

}
