package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.dvcs.DvcsUtil;
import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.codinjutsu.tools.jenkins.model.Jenkins;

public class BuildBranch extends AnAction {

    private Project project;

    @Override
    public void actionPerformed(AnActionEvent e) {
        this.project = e.getProject();
        if(this.project != null) {

        }
    }
}
