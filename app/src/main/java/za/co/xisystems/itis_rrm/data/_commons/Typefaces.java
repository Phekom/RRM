package za.co.xisystems.itis_rrm.data._commons;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.util.Hashtable;

/**
 * Created by Mauritz Mollentze on 2015/05/13.
 * Updated by Pieter Jacobs during 2016/06, 2016/07.
 */
public class Typefaces {

    // region (Private Static Final Fields)

    private static final String TAG = Typefaces.class.getSimpleName();
    private static final Hashtable<String, Typeface> cache = new Hashtable<>();

    // endregion (Private Static Final Fields)

    // region (Public Static Methods)

    public static Typeface get(Context context, String typefaceName) {
        synchronized (cache) {
            if (!cache.containsKey(typefaceName)) {
                try {
                    Typeface t = Typeface.createFromAsset(context.getAssets(), String.format("fonts/%s", typefaceName));
                    cache.put(typefaceName, t);
                } catch (Exception e) {
                    Log.e(TAG, "Could not get typeface '" + typefaceName + "' because " + e.getMessage());
                    return null;
                }
            }
            return cache.get(typefaceName);
        }
    }

    // endregion (Public Static Methods)
}
