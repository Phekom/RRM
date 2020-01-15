package za.co.xisystems.itis_rrm.data.localDB.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by georgezampetakis on 15/03/2017.
 */

public class WorkflowItemMeasure implements Serializable {

    @JsonProperty("ItemMeasureId")
    private String itemMeasureId;

    @JsonProperty("ActId")
    private int actId;

    @JsonProperty("TrackRouteId")
    private String trackRouteId;

    @JsonProperty("MeasureGroupId")
    private String measureGroupId;

    public String getItemMeasureId() {
        return itemMeasureId;
    }

    public void setItemMeasureId(String itemMeasureId) {
        this.itemMeasureId = itemMeasureId;
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

    public String getMeasureGroupId() {
        return measureGroupId;
    }

    public void setMeasureGroupId(String measureGroupId) {
        this.measureGroupId = measureGroupId;
    }

}
