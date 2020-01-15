package za.co.xisystems.itis_rrm.data.localDB.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by georgezampetakis on 15/03/2017.
 */

public class WorkflowJob implements Serializable {

    @JsonProperty("JobId")
    private String jobId;

    @JsonProperty("ActId")
    private int actId;

    @JsonProperty("TrackRouteId")
    private String trackRouteId;

    @JsonProperty("JiNo")
    private String jiNo;

    @JsonProperty("WorkflowItemEstimates")
    private ArrayList<WorkflowItemEstimate> workflowItemEstimates;

    @JsonProperty("WorkflowItemMeasures")
    private ArrayList<WorkflowItemMeasure> workflowItemMeasures;

    @JsonProperty("WorkflowJobSections")
    private ArrayList<WorkflowJobSection> workflowJobSections;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public int getActId() {
        return actId;
    }

    public void setActId(int actId) {
        this.actId = actId;
    }

    public String getTrackRouteId() {
        return trackRouteId;
    }

    public void setTrackRouteId(String trackRouteId) {
        this.trackRouteId = trackRouteId;
    }

    public ArrayList<WorkflowItemEstimate> getWorkflowItemEstimates() {
        return workflowItemEstimates;
    }

    public void setWorkflowItemEstimates(ArrayList<WorkflowItemEstimate> workflowItemEstimates) {
        this.workflowItemEstimates = workflowItemEstimates;
    }

    public ArrayList<WorkflowItemMeasure> getWorkflowItemMeasures() {
        return workflowItemMeasures;
    }

    public void setWorkflowItemMeasures(ArrayList<WorkflowItemMeasure> workflowItemMeasures) {
        this.workflowItemMeasures = workflowItemMeasures;
    }

    public String getJiNo() {
        return jiNo;
    }

    public void setJiNo(String jiNo) {
        this.jiNo = jiNo;
    }

    public ArrayList<WorkflowJobSection> getWorkflowJobSections() {
        return workflowJobSections;
    }

    public void setWorkflowJobSections(ArrayList<WorkflowJobSection> workflowJobSections) {
        this.workflowJobSections = workflowJobSections;
    }

}
