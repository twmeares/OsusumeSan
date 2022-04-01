package com.twmeares.osusumesan.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.Selection;
import android.text.Spannable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
// A portion of this code is based on a stack overflow example for creating a class that extends TextView.
@SuppressLint("AppCompatCustomView")
public class CustomTextView extends TextView
{

    //Custom text view that helps to keep track of the highlight (aka selection)
    private String TAG = "CustomTextView";
    private int highlightStart = 0;
    private int highlightEnd = 0;
    private boolean hasHighlight = false;

    public CustomTextView(Context context)
    {
        super(context);
        init();
    }

    public CustomTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init()
    {
        if (Build.VERSION.SDK_INT > 10)
            setTextIsSelectable(true);
    }

    // this whole function could probably be removed, but leaving it for reference for now.
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        Log.d(TAG, "selStart " + selStart + " selEnd " + selEnd + " has focus " + hasFocus());
        super.onSelectionChanged(selStart, selEnd);
//        if (this.hasHighlight && this.highlightStart != this.getSelectionStart()
//                && this.highlightEnd != this.getSelectionEnd()) {
//            Selection.setSelection((Spannable)this.getText(), this.highlightStart, this.highlightEnd);
//            //this.isHasHighlight = false
//            Log.d("Custom text view", "Setting the highlight again.");
//        }

    }


    public int getHighlightStart() {
        return highlightStart;
    }

    public void setHighlightStart(int highlightStart) {
        this.highlightStart = highlightStart;
    }


    public int getHighlightEnd() {
        return highlightEnd;
    }

    public void setHighlightEnd(int highlightEnd) {
        this.highlightEnd = highlightEnd;
    }

    public boolean isHasHighlight() {
        return hasHighlight;
    }

    public void setHasHighlight(boolean hasHighlight) {
        this.hasHighlight = hasHighlight;
    }

}
