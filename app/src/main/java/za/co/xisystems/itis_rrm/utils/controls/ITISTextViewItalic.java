package za.co.xisystems.itis_rrm.utils.controls;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;

import za.co.xisystems.itis_rrm.data._commons.Typefaces;


/**
 * Created by Mauritz Mollentze on 2015/05/19.
 * Updated by Pieter Jacobs during 2016/07.
 */
public class ITISTextViewItalic extends androidx.appcompat.widget.AppCompatTextView {

    // region (Private Fields)

    private Context context;
    private AttributeSet attrs;
    private int defStyleAttr;

    // endregion (Private Fields)

    // region (Public Constructors)

    public ITISTextViewItalic(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ITISTextViewItalic(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        init();
    }

    public ITISTextViewItalic(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.attrs = attrs;
        this.defStyleAttr = defStyleAttr;
        init();
    }

    // endregion (Public Constructors)

    // region (Public Methods)

    @Override
    public void setTypeface(Typeface tf) {
        tf = Typefaces.get(getContext(), "myriadproit.otf");
        super.setTypeface(tf);
    }

    // endregion (Public Methods)

    // region (Private Methods)

    private void init() {
        if (this.getTypeface() != null && !this.getTypeface().equals(Typefaces.get(getContext(), "myriadproit.otf")))
            this.setTypeface(Typefaces.get(getContext(), "myriadproit.otf"));
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setFocusable(false);
    }

    // region (Private Methods)
}
