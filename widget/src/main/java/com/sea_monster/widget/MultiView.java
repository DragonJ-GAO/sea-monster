package com.sea_monster.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by DragonJ on 14-10-8.
 */
public class MultiView extends FrameLayout {
    Map<Integer, AtomicInteger> mViewCounterMap;
    Map<Integer, View> mContentViewMap;
    View mInflateView;
    int mContentMax = 3;


    public MultiView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public MultiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MultiView(Context context) {
        super(context);
    }

    private void init(AttributeSet attrs) {
        mViewCounterMap = new HashMap<Integer, AtomicInteger>();
        mContentViewMap = new HashMap<Integer, View>();
    }

    public View inflate(int layoutId) {
        View result = null;

        if (mInflateView != null)
            mInflateView.setVisibility(View.GONE);

        if (mContentViewMap.containsKey(layoutId)) {
            result = mContentViewMap.get(layoutId);
            mInflateView = result;
            mViewCounterMap.get(layoutId).incrementAndGet();
        }

        if (result != null) {
            if (result.getVisibility() == View.GONE)
                result.setVisibility(View.VISIBLE);

            Log.d(View.VIEW_LOG_TAG, result.toString());
            for (Map.Entry<Integer, AtomicInteger> item : mViewCounterMap.entrySet()) {
                Log.d(View.VIEW_LOG_TAG, item.getKey() + ":" + item.getValue().get());
            }

            return result;
        }

        recycle();

        result = LayoutInflater.from(getContext()).inflate(layoutId, null);
        super.addView(result);

        mContentViewMap.put(layoutId, result);
        mViewCounterMap.put(layoutId, new AtomicInteger());

        mInflateView = result;

        Log.d(View.VIEW_LOG_TAG, result.toString());
        for (Map.Entry<Integer, AtomicInteger> item : mViewCounterMap.entrySet()) {
            Log.d(View.VIEW_LOG_TAG, item.getKey() + ":" + item.getValue().get());
        }

        return result;
    }

    private void recycle() {
        if (mInflateView == null)
            return;

        int count = getChildCount();
        if (count >= mContentMax) {
            Map.Entry<Integer, AtomicInteger> min = null;

            for (Map.Entry<Integer, AtomicInteger> item : mViewCounterMap.entrySet()) {
                if (min == null)
                    min = item;

                min = min.getValue().get() > item.getValue().get() ? item : min;
            }

            mViewCounterMap.remove(min.getKey());
            View view = mContentViewMap.remove(min.getKey());
            removeView(view);
        }
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        throw new UnsupportedOperationException();
    }
}
