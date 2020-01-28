package za.co.xisystems.itis_rrm.utils.controls;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import za.co.xisystems.itis_rrm.R;
import za.co.xisystems.itis_rrm.data._commons.Typefaces;
import za.co.xisystems.itis_rrm.utils.ColorUtil;

/**
 * Created by Pieter Jacobs on 2016/07/18.
 * Updated by Pieter Jacobs during 2016/07.
 */
public class ItisTextViewSilverBackground extends EditText {

    // region (Private Fields)

    private Context context;
    private AttributeSet attrs;
    private int defStyleAttr;

    // endregion (Private Fields)

    // region (Public Constructors)

    public ItisTextViewSilverBackground(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ItisTextViewSilverBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        init();
    }

    public ItisTextViewSilverBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.attrs = attrs;
        this.defStyleAttr = defStyleAttr;
        init();
    }

    //endregion (Public Constructors)

    // region (Public Methods)

    @Override
    public void setTypeface(Typeface tf) {
        tf = Typefaces.get(getContext(), "MyriadPro-Regular.otf");
        super.setTypeface(tf);
    }

    // endregion (Public Methods)

    // region (Private Methods)

    private void init() {
        if((this.getTypeface() != null) && (!this.getTypeface().equals(Typefaces.get(getContext(), "MyriadPro-Regular.otf"))))
            this.setTypeface(Typefaces.get(getContext(), "MyriadPro-Regular.otf"));

        this.setBackgroundColor(ColorUtil.getTextColor(getContext(), android.R.color.transparent));
        this.setTextColor(ColorUtil.getTextColor(getContext(), R.color.itis_gray));
        this.setFocusable(false);
    }

    // endregion (Private Methods)
}
