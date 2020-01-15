package za.co.xisystems.itis_rrm.data.localDB.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by georgezampetakis on 21/03/2017.
 */

public class WorkflowJobSection implements Serializable {

    @JsonProperty("JobId")
    private String jobId;

    @JsonProperty("JobSectionId")
    private String jobSectionId;

    @JsonProperty("ProjectSectionId")
    private String projectSectionId;

    @JsonProperty("RecordVersion")
    private int recordVersion;

    @JsonProperty("RecordSynchStateId")
    private int recordSynchStateId;

    @JsonProperty("StartKm")
    private double startKm;

    @JsonProperty("EndKm")
    private double endKm;

    public double getEndKm() {
        return endKm;
    }

    public void setEndKm(double endKm) {
        this.endKm = endKm;
    }

    public String getJobSectionId() {
        return jobSectionId;
    }

    public void setJobSectionId(String jobSectionId) {
        this.jobSectionId = jobSectionId;
    }

    public String getProjectSectionId() {
        return projectSectionId;
    }

    public void setProjectSectionId(String projectSectionId) {
        this.projectSectionId = projectSectionId;
    }

    public int getRecordSynchStateId() {
        return recordSynchStateId;
    }

    public void setRecordSynchStateId(int recordSynchStateId) {
        this.recordSynchStateId = recordSynchStateId;
    }

    public double getStartKm() {
        return startKm;
    }

    public void setStartKm(double startKm) {
        this.startKm = startKm;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public int getRecordVersion() {
        return recordVersion;
    }

    public void setRecordVersion(int recordVersion) {
        this.recordVersion = recordVersion;
    }

}
