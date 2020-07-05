package za.co.xisystems.itis_rrm.utils;

import android.content.Context;

import androidx.core.content.ContextCompat;

/**
 * Created by Pieter Jacobs on 2016/07/19.
 * Updated by Pieter Jacobs during 2016/07.
 */
public class ColorUtil {

    public static int getTextColor(Context context, int id) {
        return ContextCompat.getColor(context, id);
    }

    // endregion (Public Static Methods)
}
