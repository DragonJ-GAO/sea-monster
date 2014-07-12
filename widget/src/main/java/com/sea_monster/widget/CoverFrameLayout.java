package com.sea_monster.widget;

import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by DragonJ on 14-7-12.
 */
public class CoverFrameLayout extends FrameLayout {
    View mCoverView;
    View mTriggerView;
    int mCoverViewResId;
    int mTriggerViewResId;
    int mTriggerLimit;
    float mTriggerCenterX = 0;
    float mTriggerCenterY = 0;
    OnTriggeredTouchListener mTriggeredTouchListener;

    public CoverFrameLayout(android.content.Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CoverFrameLayout);

        mCoverViewResId = a.getResourceId(R.styleable.CoverFrameLayout_coverView, 0);
        mTriggerViewResId = a.getResourceId(R.styleable.CoverFrameLayout_triggerView, 0);
        mTriggerLimit = a.getInt(R.styleable.CoverFrameLayout_triggerLimit, 200);

        if (mCoverViewResId == 0 || mTriggerViewResId == 0)
            throw new RuntimeException("CoverView or TriggerView not define");
    }


    @Override
    protected void onAttachedToWindow() {
        mCoverView = findViewById(mCoverViewResId);
        mTriggerView = findViewById(mTriggerViewResId);
        mTriggerView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        Rect rect = new Rect();
        mTriggerView.getHitRect(rect);
        mTriggerCenterX = rect.centerX();
        mTriggerCenterY = rect.centerY();
        super.onAttachedToWindow();
    }

    boolean mHasTrigger;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d(this.getClass().getCanonicalName(), ev.toString());

        if (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP) {
            if (mHasTrigger) {
                MotionEvent event = MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(), ev.getAction(), mTriggerCenterX, mTriggerCenterY, ev.getMetaState());
                mTriggerView.dispatchTouchEvent(event);

                if (mTriggeredTouchListener != null) {
                    mTriggeredTouchListener.OnTriggeredTouchEvent(event);
                }

                mHasTrigger = false;
                if ( mCoverView.getVisibility() == View.VISIBLE)
                    mCoverView.setVisibility(View.GONE);
            }
        }

        if (mHasTrigger) {
            MotionEvent event = MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(), ev.getAction(), mTriggerCenterX, mTriggerCenterY, ev.getMetaState());
            mTriggerView.dispatchTouchEvent(event);

            if (mTriggeredTouchListener != null) {
                mTriggeredTouchListener.OnTriggeredTouchEvent(event);
            }

            if (ev.getEventTime() - ev.getDownTime()>mTriggerLimit && mCoverView.getVisibility() == View.GONE)
                mCoverView.setVisibility(View.VISIBLE);
        }
        return true;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(this.getClass().getCanonicalName(), ev.toString());
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            Rect rect = new Rect();
            mTriggerView.getHitRect(rect);
            mHasTrigger = rect.contains((int) ev.getX(), (int) ev.getY());
        }

        if (mHasTrigger) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setOnTriggeredTouchListener(OnTriggeredTouchListener listener) {
        mTriggeredTouchListener = listener;
    }

    public interface OnTriggeredTouchListener {
        public void OnTriggeredTouchEvent(MotionEvent event);
    }
}
