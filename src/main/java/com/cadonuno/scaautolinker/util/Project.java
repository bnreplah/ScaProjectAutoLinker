package com.cadonuno.scaautolinker.util;

import java.util.Objects;

public final class Project {
    private final String guid;
    private final String siteId;
    private final String name;
    private final String languages;
    private final Workspace workspace;
    private String issueOnUpdate;

    public Project(String guid, String siteId, String name, String languages, Workspace workspace) {
        this.guid = guid;
        this.siteId = siteId;
        this.name = name;
        this.languages = languages;
        this.workspace = workspace;
    }

    public boolean isNotContainerScan() {
        return !"OS".equals(languages);
    }

    public String getFullName() {
        return workspace.getName() + "->" + name;
    }

    public String getGuid() {
        return guid;
    }

    public String getSiteId() {
        return siteId;
    }

    public String getName() {
        return name;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public String getIssueOnUpdate() {
        return issueOnUpdate;
    }

    public void setIssueOnUpdate(String issueOnUpdate) {
        this.issueOnUpdate = issueOnUpdate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Project) obj;
        return Objects.equals(this.guid, that.guid) &&
                Objects.equals(this.siteId, that.siteId) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.languages, that.languages) &&
                Objects.equals(this.workspace, that.workspace) &&
                Objects.equals(this.issueOnUpdate, that.issueOnUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guid, siteId, name, languages, workspace, issueOnUpdate);
    }

    @Override
    public String toString() {
        return "Project[" +
                "guid=" + guid + ", " +
                "siteId=" + siteId + ", " +
                "name=" + name + ", " +
                "languages=" + languages + ", " +
                "workspace=" + workspace + ", " +
                "issueToUpload=" + issueOnUpdate + ']';
    }

    public void concatenateIssue(String issueToConcatenate) {
        issueOnUpdate += "\n" + issueToConcatenate;
    }
}
