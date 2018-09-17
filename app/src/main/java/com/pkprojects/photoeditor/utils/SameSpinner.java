package com.pkprojects.photoeditor.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AdapterView;

/** Spinner extension that calls onItemSelected even when the selection is the same as its previous value */
public class SameSpinner extends android.support.v7.widget.AppCompatSpinner {

    public SameSpinner(Context context)  { super(context); }

    public SameSpinner(Context context, AttributeSet attrs) { super(context, attrs); }

    public SameSpinner(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    @Override
    public void setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if(sameSelected) {
            AdapterView.OnItemSelectedListener listener = getOnItemSelectedListener();
            if(listener != null) {
                listener.onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            }
        }
    }

    @Override
    public void setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if(sameSelected) {
            AdapterView.OnItemSelectedListener listener = getOnItemSelectedListener();
            if(listener != null) {
                listener.onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            }
        }
    }
}
