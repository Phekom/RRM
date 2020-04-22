package za.co.xisystems.itis_rrm.utils;

import android.content.Context;
import android.os.Build;

import androidx.core.content.ContextCompat;

/**
 * Created by Pieter Jacobs on 2016/07/19.
 * Updated by Pieter Jacobs during 2016/07.
 */
public class ColorUtil {

    // region (Public Static Methods)

    public static int getTextColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;

        return ContextCompat.getColor(context, id);

    }

    // endregion (Public Static Methods)
}
