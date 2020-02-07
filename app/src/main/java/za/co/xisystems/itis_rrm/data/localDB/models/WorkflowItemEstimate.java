package za.co.xisystems.itis_rrm.data.localDB.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by georgezampetakis on 15/03/2017.
 */

public class WorkflowItemEstimate implements Serializable {

    @SerializedName("EstimateId")
    private String estimateId;

    @SerializedName("ActId")
    private int actId;

    @SerializedName("TrackRouteId")
    private String trackRouteId;

    @SerializedName("WorkflowEstimateWorks")
    private ArrayList<WorkflowEstimateWork> workflowEstimateWorks;

    public String getEstimateId() {
        return estimateId;
    }

    public void setEstimateId(String estimateId) {
        this.estimateId = estimateId;
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

    public ArrayList<WorkflowEstimateWork> getWorkflowEstimateWorks() {
        return workflowEstimateWorks;
    }

    public void setWorkflowEstimateWorks(ArrayList<WorkflowEstimateWork> workflowEstimateWorks) {
        this.workflowEstimateWorks = workflowEstimateWorks;
    }

}
