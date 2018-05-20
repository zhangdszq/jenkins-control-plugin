package org.codinjutsu.tools.jenkins.util;

import com.intellij.dvcs.DvcsUtil;
import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class VcsUtil {
    public static String getCurrentBranch(@NotNull Project project) {
        String branchName = null;
        VcsRepositoryManager vcs = ServiceManager.getService(project, VcsRepositoryManager.class);
        VirtualFile selectedFile = DvcsUtil.getSelectedFile(project);
        if (selectedFile != null) {
            Repository repository = vcs.getRepositoryForFile(selectedFile);
            try {
                branchName = repository.getCurrentBranchName();
            } catch (NullPointerException e1) {
                branchName = null;
            }
        }
        return branchName;
    }
}
