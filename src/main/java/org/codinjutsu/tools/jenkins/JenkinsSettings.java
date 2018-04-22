/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.security.JenkinsVersion;
import org.jetbrains.annotations.NotNull;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.impl.PasswordSafeImpl;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

@State(
      name = "Jenkins.Settings",
      storages = {
            @Storage(StoragePathMacros.WORKSPACE_FILE)
      }
)
public class JenkinsSettings implements PersistentStateComponent<JenkinsSettings.State> {

    private static final Logger LOG = Logger.getInstance(JenkinsSettings.class.getName());

    public static final String JENKINS_SETTINGS_PSW_KEY = "JENKINS_SETTINGS_PSW_KEY";

    private State myState = new State();

    public static JenkinsSettings getSafeInstance(Project project) {
        JenkinsSettings settings = ServiceManager.getService(project, JenkinsSettings.class);
        return settings != null ? settings : new JenkinsSettings();
    }

    @Override
    public State getState() {
//        ensurePasswordIsStored();
        return myState;
    }

    @Override
    public void loadState(State state) {
        XmlSerializerUtil.copyBean(state, myState);
    }

    public String getUsername() {
        return myState.username;
    }

    public void setUsername(String username) {
        myState.username = username;
    }

    public String getCrumbData() {
        return myState.crumbData;
    }

    public void setCrumbData(String crumbData) {
        myState.crumbData = crumbData;
    }

    public String getPassword() {
        PasswordSafeImpl passwordSafe = (PasswordSafeImpl) PasswordSafe.getInstance();
        String psw = passwordSafe.getPassword(getCredentialAttributes(JENKINS_SETTINGS_PSW_KEY));

        return StringUtils.defaultIfEmpty(psw, "");
    }

    public void setPassword(String psw) {
        PasswordSafe.getInstance().setPassword(getCredentialAttributes(JENKINS_SETTINGS_PSW_KEY), StringUtils.isNotBlank(psw) ? psw : "");
    }

    @NotNull
    private static CredentialAttributes getCredentialAttributes(@NotNull String key) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("Jenkins password", key), key, JenkinsAppSettings.class);
    }

    public void addFavorite(List<Job> jobs) {
        for (Job job : jobs) {
            FavoriteJob favoriteJob = new FavoriteJob();
            favoriteJob.name = job.getName();
            favoriteJob.url = job.getUrl();
            myState.favoriteJobs.add(favoriteJob);
        }
    }

    public boolean isAFavoriteJob(String jobName) {
        for (FavoriteJob favoriteJob : myState.favoriteJobs) {
            if (StringUtils.equals(jobName, favoriteJob.name)) {
                return true;
            }
        }
        return false;
    }

    public void removeFavorite(List<Job> selectedJobs) {//TODO need to refactor
        for (Job selectedJob : selectedJobs) {
            myState.favoriteJobs.removeIf(favoriteJob -> StringUtils.equals(selectedJob.getName(), favoriteJob.name));
        }
    }

    public List<FavoriteJob> getFavoriteJobs() {
        return myState.favoriteJobs;
    }

    public boolean isFavoriteViewEmpty() {
        return myState.favoriteJobs.isEmpty();
    }

    public void setLastSelectedView(String viewName) {
        myState.lastSelectedView = viewName;
    }

    public String getLastSelectedView() {
        return myState.lastSelectedView;
    }

    public boolean isSecurityMode() {
        return StringUtils.isNotBlank(getUsername());
    }

    public JenkinsVersion getVersion() {
        return this.myState.jenkinsVersion;
    }

    public void setVersion(JenkinsVersion jenkinsVersion) {
        this.myState.jenkinsVersion = jenkinsVersion;
    }

    public static class State {

        public static final String RESET_STR_VALUE = "";

        public String username = RESET_STR_VALUE;

        public String crumbData = RESET_STR_VALUE;

        public String lastSelectedView;

        public List<FavoriteJob> favoriteJobs = new LinkedList<>();

        public JenkinsVersion jenkinsVersion = JenkinsVersion.VERSION_1;
    }

    @Tag("favorite")
    public static class FavoriteJob {

        @Attribute("name")
        public String name;

        @Attribute("url")
        public String url;
    }
}