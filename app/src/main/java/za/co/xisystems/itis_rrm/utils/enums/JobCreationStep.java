package za.co.xisystems.itis_rrm.utils.enums;

/**
 * JobCreationStep.java - Enum containing the Job Creation Steps.
 * @author   Pieter Jacobs
 * @version  1.0
 * @since    2016/10/19
 */
public enum JobCreationStep {
    ITEM {
        public String toString() {
            return "Item";
        }
    },

    LOCATION {
        public String toString() {
            return "Location/Qty";
        }
    },

    DUE_DATE {
        public String toString() {
            return "Due Date";
        }
    },

    SUBMIT {
        public String toString() {
            return "Submit";
        }
    }
}
