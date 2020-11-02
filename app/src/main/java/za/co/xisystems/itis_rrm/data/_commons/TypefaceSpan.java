package za.co.xisystems.itis_rrm.data._commons;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.util.LruCache;

/**
 * Created by Mauritz Mollentze on 2015/05/13.
 * Updated by Pieter Jacobs during 2016/06, 2016/07.
 */
public class TypefaceSpan extends MetricAffectingSpan {

    // region (Private Static Fields)

    private static LruCache<String, Typeface> sTypefaceCache = new LruCache<>(12);

    // endregion (Private Static Fields)

    // region (Private Fields)

    private Typeface mTypeface;

    // endregion (Private Fields)

    // region (Public Constructors)

    public TypefaceSpan(Context context, String typefaceName) {
        mTypeface = sTypefaceCache.get(typefaceName);

        if (null == mTypeface) {
            mTypeface = Typeface.createFromAsset(context.getApplicationContext().getAssets(), String.format("fonts/%s", typefaceName));

            // Cache the loaded Typeface
            sTypefaceCache.put(typefaceName, mTypeface);
        }
    }

    public TypefaceSpan(String androidTypeFaceStyle,int typefaceStyle) {
        mTypeface = sTypefaceCache.get(androidTypeFaceStyle);

        if (null == mTypeface) {
            mTypeface = Typeface.create(androidTypeFaceStyle,typefaceStyle);
            sTypefaceCache.put(androidTypeFaceStyle, mTypeface);
        }
    }

    // endregion (Public Constructors)

    // region (Public Methods)

    @Override
    public void updateMeasureState(TextPaint p) {
        p.setTypeface(mTypeface);

        // Note: This flag is required for proper typeface rendering
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(mTypeface);

        // Note: This flag is required for proper typeface rendering
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    // endregion (Public Methods)
}
