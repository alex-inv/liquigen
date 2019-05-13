package com.alivanov.intellij.plugins.liquigen.wrapper;

import com.intellij.ide.scratch.RootType;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class EditorWrapper {

    private EditorWrapper() {

    }

    public static void openInEditor(Project project, String filename, String text) {
        ScratchRootType scratchType = RootType.findByClass(ScratchRootType.class);
        VirtualFile scratchFile = scratchType.createScratchFile(project, filename, StdLanguages.XML, text);

        if (scratchFile != null) {
            FileEditorManager.getInstance(project).openFile(scratchFile, true);
        }
    }

}
