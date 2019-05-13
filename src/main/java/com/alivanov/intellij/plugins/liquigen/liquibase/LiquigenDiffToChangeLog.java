package com.alivanov.intellij.plugins.liquigen.liquibase;

import com.alivanov.intellij.plugins.liquigen.Extension;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.serializer.ChangeLogSerializerFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintStream;

public class LiquigenDiffToChangeLog extends DiffToChangeLog {

    public LiquigenDiffToChangeLog(final DiffResult diffResult, final DiffOutputControl diffOutputControl) {
        super(diffResult, diffOutputControl);
    }

    public void print(final PrintStream out, Extension extension) throws ParserConfigurationException, IOException, DatabaseException {
        this.print(out, ChangeLogSerializerFactory.getInstance().getSerializer(extension.getExtension()));
    }
}
