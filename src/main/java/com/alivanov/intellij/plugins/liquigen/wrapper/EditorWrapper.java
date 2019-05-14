package com.alivanov.intellij.plugins.liquigen.wrapper;

import com.alivanov.intellij.plugins.liquigen.Extension;
import com.alivanov.intellij.plugins.liquigen.settings.LiquigenConfig;
import com.intellij.ide.scratch.RootType;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class EditorWrapper {

    private EditorWrapper() {

    }

    public static void openInEditor(Project project, String filename, String text) {
        Extension extension = LiquigenConfig.getInstance(project).getExtension();

        ScratchRootType scratchType = RootType.findByClass(ScratchRootType.class);
        VirtualFile scratchFile = scratchType.createScratchFile(project, filename + "." + extension.getExtension(),
                Language.findLanguageByID(extension.getLanguageId()), text);

        if (scratchFile != null) {
            FileEditorManager.getInstance(project).openFile(scratchFile, true);
        }
    }

}
