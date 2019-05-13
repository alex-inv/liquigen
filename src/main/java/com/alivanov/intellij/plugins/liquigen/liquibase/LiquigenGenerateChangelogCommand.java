package com.alivanov.intellij.plugins.liquigen.liquibase;

import com.alivanov.intellij.plugins.liquigen.Extension;
import liquibase.command.CommandResult;
import liquibase.command.core.GenerateChangeLogCommand;
import liquibase.diff.DiffResult;

import java.io.PrintStream;

public class LiquigenGenerateChangelogCommand extends GenerateChangeLogCommand {

    private Extension extension;

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
        changeLogWriter.setChangeSetContext(getContext());

        PrintStream outputStream = getOutputStream();
        if (outputStream == null) {
            outputStream = System.out;
        }
        changeLogWriter.print(outputStream, getExtension());

        return new CommandResult("OK");

    }

}
