package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.logic.ExecutorService;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.util.HtmlUtil;
import org.codinjutsu.tools.jenkins.util.VcsUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.BuildParamDialog;
import org.codinjutsu.tools.jenkins.view.ConfirmDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BuildCurrentBranch extends AnAction implements DumbAware {

    public static final int BUILD_STATUS_UPDATE_DELAY = 1;
    private static final Logger LOG = Logger.getLogger(RunBuildAction.class.getName());
    private static final String BRANCH_PARAMETER_NAME = "Branch";
    private static final String ACCEPT_PARAMETER_NAME = "Accept";

    private final BrowserPanel browserPanel;
    private Project project;

    public BuildCurrentBranch(BrowserPanel browserPanel) {
        super("Build current branch", "Build current branch", GuiUtil.loadIcon("execute.png"));
        this.browserPanel = browserPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            List<Job> jobs = browserPanel.getAllSelectedJobs();
            String branch = VcsUtil.getCurrentBranch(project);
            for(Job job: jobs) {
                Task.Backgroundable task = new Task.Backgroundable(project, "Running Build", false) {

                            @Override
                            public void onSuccess() {
                                ExecutorService.getInstance(project).getExecutor().schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        GuiUtil.runInSwingThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                final Job newJob = browserPanel.getJob(job.getName());
                                                browserPanel.loadJob(newJob);
                                            }
                                        });
                                    }
                                }, BUILD_STATUS_UPDATE_DELAY, TimeUnit.SECONDS);

                            }

                            @Override
                            public void run(@NotNull ProgressIndicator progressIndicator) {
                                progressIndicator.setIndeterminate(true);
                                RequestManager requestManager = browserPanel.getJenkinsManager();
                                if (job.hasParameters()) {
                                    if (branch != null) {
                                        JobParameter branchParam = new JobParameter();
                                        branchParam.setDefaultValue(branch);
                                        job.setParameter(branchParam);
                                        HashMap<String, String> valueByNameMap = new HashMap<String, String>();
                                        valueByNameMap.put(BRANCH_PARAMETER_NAME, branch);
                                        Boolean accept = true;
                                        if (job.hasParameter(ACCEPT_PARAMETER_NAME)) {
                                            accept = ConfirmDialog.confirm(String.format("Are you sure, you want to build \"%s\" on \"%s\"?", branch, job.getName()));
                                            valueByNameMap.put(ACCEPT_PARAMETER_NAME, (accept ? "true" : ""));
                                        }
                                        if (accept) {
                                            requestManager.runParameterizedBuild(job, JenkinsAppSettings.getSafeInstance(project), valueByNameMap);
                                            browserPanel.notifyInfoJenkinsToolWindow(
                                                    HtmlUtil.createHtmlLinkMessage(
                                                            job.getName() + " \"" + branch + "\" build is on going",
                                                            job.getUrl()
                                                    )
                                            );
                                        } else {
                                            browserPanel.notifyErrorJenkinsToolWindow("You have to make up your mind for the build");
                                        }
                                    } else {
                                        BuildParamDialog.showDialog(job, JenkinsAppSettings.getSafeInstance(project), requestManager, new BuildParamDialog.RunBuildCallback() {
                                            public void notifyOnOk(Job job) {
                                                notifyOnGoingMessage(job);
                                                browserPanel.loadJob(job);
                                            }

                                            public void notifyOnError(Job job, Exception ex) {
                                                browserPanel.notifyErrorJenkinsToolWindow("Build '" + job.getName() + "' cannot be run: " + ex.getMessage());
                                                browserPanel.loadJob(job);
                                            }
                                        });
                                    }
                                } else {
                                    requestManager.runBuild(job, JenkinsAppSettings.getSafeInstance(project));
                                }
                            }
                        };

                if (SwingUtilities.isEventDispatchThread()) {
                    ProgressManager.getInstance().run(task);
                } else {
                    SwingUtilities.invokeLater(() -> ProgressManager.getInstance().run(task));
                }
            }

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            browserPanel.notifyErrorJenkinsToolWindow("Build cannot be run: " + ex.getMessage());
        }
    }

    private void notifyOnGoingMessage(Job job) {
        browserPanel.notifyInfoJenkinsToolWindow(HtmlUtil.createHtmlLinkMessage(
                job.getName() + " build is on going",
                job.getUrl()));
    }

    @Override
    public void update(AnActionEvent event) {
        project = ActionUtil.getProject(event);
        List<Job> selectedJobs = browserPanel.getAllSelectedJobs();
        String currentBranch = VcsUtil.getCurrentBranch(project);
        if(!selectedJobs.isEmpty()){
            if(selectedJobs.size() == 1){
                Job selectedJob = selectedJobs.get(0);
                event.getPresentation().setVisible(currentBranch != null && selectedJob != null && selectedJob.hasParameters() && selectedJob.hasParameter(BRANCH_PARAMETER_NAME));
            } else {
                event.getPresentation().setVisible(true);
            }
            if (event.getPresentation().isVisible()) {
                event.getPresentation().setText(String.format("Build '%s'", currentBranch));
            }
        } else {
            event.getPresentation().setVisible(false);
        }
    }

}
