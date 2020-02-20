package za.co.xisystems.itis_rrm.data.deleteafter.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import za.co.xisystems.itis_rrm.R;
import za.co.xisystems.itis_rrm.data._commons.Typefaces;


/**
 * Created by Mauritz Mollentze on 2014/12/04.
 * Updated by Pieter Jacobs during 2016/07.
 */
public class CaptureWorkActionItemsAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> data;
    private int selection = 0;
    private View lastSelectedRow;

    public CaptureWorkActionItemsAdapter(Context context, ArrayList<String> data) {
        this.context = context;
        this.data = data;
    }


    public void setSelection(int mSelection, View selectedItemView) {
        this.selection = mSelection;
        if (selectedItemView != null && lastSelectedRow != null && selectedItemView != lastSelectedRow) {
            lastSelectedRow.setBackgroundColor(Color.TRANSPARENT);
            selectedItemView.setBackgroundColor(context.getResources().getColor(R.color.sanral_burnt_orange));
        }
        this.lastSelectedRow = selectedItemView;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.capture_work_action_item, null);
        }

        if (selection == position) {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.sanral_burnt_orange));
            lastSelectedRow = convertView;
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        TextView actionTextView = (TextView) convertView.findViewById(R.id.capture_work_action_textView);
        if (actionTextView.getTypeface() != null && !actionTextView.getTypeface().equals(Typefaces.get(context, "MyriadPro-Regular.otf")))
            actionTextView.setTypeface(Typefaces.get(context, "MyriadPro-Regular.otf"));
        actionTextView.setText(data.get(position));

        if (selection == position)
            actionTextView.setTextColor(Color.WHITE);
        else
            actionTextView.setTextColor(context.getResources().getColor(R.color.itis_gray));

        return convertView;
    }

    public void swapItems(ArrayList<String> stringArrayList) {
        this.data = stringArrayList;
        notifyDataSetChanged();
    }

}
