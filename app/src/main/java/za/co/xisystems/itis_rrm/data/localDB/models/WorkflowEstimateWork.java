package za.co.xisystems.itis_rrm.data.localDB.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import static za.co.xisystems.itis_rrm.data.localDB.models.EqualUtils.equal;


/**
 * Created by georgezampetakis on 15/03/2017.
 */

public class WorkflowEstimateWork implements Serializable {

    @JsonProperty("WorksId")
    private String worksId;

    @JsonProperty("ActId")
    private int actId;

    @JsonProperty("TrackRouteId")
    private String trackRouteId;

    @JsonProperty("EstimateId")
    private String estimateId;

    @JsonProperty("RecordVersion")
    private int recordVersion;

    @JsonProperty("RecordSynchStateId")
    private int recordSynchStateId;

    public String getTrackRouteId() {
        return trackRouteId;
    }

    public void setTrackRouteId(String trackRouteId) {
        this.trackRouteId = trackRouteId;
    }

    public String getWorksId() {
        return worksId;
    }

    public void setWorksId(String worksId) {
        this.worksId = worksId;
    }

    public int getActId() {
        return actId;
    }

    public void setActId(int actId) {
        this.actId = actId;
    }

    public String getEstimateId() {
        return estimateId;
    }

    public void setEstimateId(String estimateId) {
        this.estimateId = estimateId;
    }

    public int getRecordVersion() {
        return recordVersion;
    }

    public void setRecordVersion(int recordVersion) {
        this.recordVersion = recordVersion;
    }

    public int getRecordSynchStateId() {
        return recordSynchStateId;
    }

    public void setRecordSynchStateId(int recordSynchStateId) {
        this.recordSynchStateId = recordSynchStateId;
    }

    public static boolean equals(WorkflowEstimateWork expected, WorkflowEstimateWork actual) {
        if (expected == null && actual == null) return true;
        if (expected == null || actual == null) return false;

        return equal(expected.worksId, actual.worksId)
                && expected.actId == actual.actId
                && equal(expected.trackRouteId, actual.trackRouteId)
                && equal(expected.estimateId, actual.estimateId)
                && expected.recordVersion == actual.recordVersion
                && expected.recordSynchStateId == actual.recordSynchStateId;
    }
}
