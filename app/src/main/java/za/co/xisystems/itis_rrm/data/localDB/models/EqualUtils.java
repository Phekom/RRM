package za.co.xisystems.itis_rrm.data.localDB.models;

public final class EqualUtils {
    public static final boolean equal(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
