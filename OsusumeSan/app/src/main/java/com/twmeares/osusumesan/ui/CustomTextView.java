package com.twmeares.osusumesan.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
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


    @Override
    public void scrollTo(int x, int y) {
        // Disable scrolling. Aka do nothing here
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        double topPadding = this.getPaddingTop();
        int maxLines = (int)(this.getHeight() - topPadding)/this.getLineHeight() - 1; //minus one to avoid half cutoff rows
        this.setMaxLines(maxLines);
        //int lastPos = this.getLayout().getLineEnd(Math.min(this.getMaxLines() - 1, this.getLayout().getLineCount()-1));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        // Try for a width based on our minimum
//        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
//        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
//
//        // Whatever the width ends up being, ask for a height that would let the pie
//        // get as big as it can
//        int minh = getSuggestedMinimumHeight() + getPaddingBottom() + getPaddingTop();
//        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);
//
//        setMeasuredDimension(w, h);
//    }

}
