package com.twmeares.osusumesan.ui;

import static java.lang.Math.abs;

import android.graphics.Color;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class MovementMethod extends LinkMovementMethod {

    private static MovementMethod sInstance;

    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new MovementMethod();
        return sInstance;
    }

    @Override
    public boolean canSelectArbitrarily () {
        return true;
    }

    @Override
    public void initialize(TextView widget, Spannable text) {
        Selection.setSelection(text, text.length());
    }

    @Override
    public void onTakeFocus(TextView view, Spannable text, int dir) {
        if ((dir & (view.FOCUS_FORWARD | view.FOCUS_DOWN)) != 0) {
            if (view.getLayout() == null) {
                // This shouldn't be null, but do something sensible if it is.
                Selection.setSelection(text, text.length());
            }
        } else {
            Selection.setSelection(text, text.length());
        }
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        // This code was inspired by various stack overflow post and adapted to this projects needs
        // This solves the issue of two clickable spans that are "overlapping" and ensures the
        // click event goes to the span with the closest center to the click.
        // https://stackoverflow.com/questions/9274331/clickablespan-strange-behavioronclick-called-when-clicking-empty-space
        // Additional changes to support selectable text (copy/paste menu) taken from
        // https://stackoverflow.com/questions/15836306/can-a-textview-be-selectable-and-contain-links

        //Log.d("MovementMethod", "movement action = " + MotionEvent.actionToString(action));

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int offset = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] spans =  buffer.getSpans(offset, offset, ClickableSpan.class);

            //no clickable span, let the event propogate
            if (spans.length == 0) {
                //fall through case
                //return super.onTouchEvent(widget, buffer, event);
                return Touch.onTouchEvent(widget, buffer, event);
            }

            //only handle the clickable span on action_down to avoid calling it twice.
            if (spans.length > 1) {
                //determine which span is really closer to the click
                int startSpan0 = buffer.getSpanStart(spans[0]);
                int endSpan0 = buffer.getSpanEnd(spans[0]);
                int startSpan1 = buffer.getSpanStart(spans[1]);
                int endSpan1 = buffer.getSpanEnd(spans[1]);

                // if the span stretches past the line but actually ends at the last position of
                // the line then the endX for the span will be 0. This needs to be corrected by
                // setting it equal to the x value for the end of the line.
                int lineEnd = layout.getLineEnd(line);

                double startXOfSpan0 = layout.getPrimaryHorizontal(startSpan0);
                double endXOfSpan0;
                if (lineEnd == endSpan0){
                    endXOfSpan0 = layout.getLineRight(line);
                } else {
                    endXOfSpan0 = layout.getPrimaryHorizontal(endSpan0);
                }


                double startXOfSpan1 = layout.getPrimaryHorizontal(startSpan1);
                double endXOfSpan1;
                if (lineEnd == endSpan1){
                    endXOfSpan1 = layout.getLineRight(line);
                } else {
                    endXOfSpan1 = layout.getPrimaryHorizontal(endSpan1);
                }

                double centerSpan0 = (endXOfSpan0 + startXOfSpan0) / 2;
                double centerSpan1 = (endXOfSpan1 + startXOfSpan1) / 2;

                // How far is the center from the click?
                double distToCenter0 = abs(centerSpan0 - x);
                double distToCenter1 = abs(centerSpan1 - x);
                if (distToCenter0 > distToCenter1) {
                    if (action == MotionEvent.ACTION_UP) {
                        spans[1].onClick(widget);
                        Selection.setSelection(buffer, startSpan1, endSpan1);
                        CustomTextView ctv = (CustomTextView) widget;
                        ctv.setHasHighlight(true);
                        ctv.setHighlightStart(startSpan1);
                        ctv.setHighlightEnd(endSpan1);
                    }
//                    else if (action == MotionEvent.ACTION_DOWN) {
                        // Do nothing on MotionEvent.ACTION_DOWN
//                    }
                    return true;
                }
            }

            // default case when there is only one span found.
            int startSpan0 = buffer.getSpanStart(spans[0]);
            int endSpan0 = buffer.getSpanEnd(spans[0]);
            if (action == MotionEvent.ACTION_UP) {
                spans[0].onClick(widget);
                Selection.setSelection(buffer, startSpan0, endSpan0);
                CustomTextView ctv = (CustomTextView) widget;
                ctv.setHasHighlight(true);
                ctv.setHighlightStart(startSpan0);
                ctv.setHighlightEnd(endSpan0);
            }
//            else if (action == MotionEvent.ACTION_DOWN) {
                // Do nothing on MotionEvent.ACTION_DOWN
//            }
            return true;

        }

        //return super.onTouchEvent(widget, buffer, event);
        return Touch.onTouchEvent(widget, buffer, event);
    }

}