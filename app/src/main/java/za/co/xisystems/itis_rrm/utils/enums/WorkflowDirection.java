package za.co.xisystems.itis_rrm.utils.enums;

/**
 * Created by Mauritz Mollentze on 2015/03/13.
 * Updated by Pieter Jacobs during 2016/02, 2016/06.
 */
public enum WorkflowDirection {
    NEXT(0),
    FAIL(1),
    ERROR(2),
    UNSUPPORTED(3);

    private int value;

    WorkflowDirection(int value) {
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
