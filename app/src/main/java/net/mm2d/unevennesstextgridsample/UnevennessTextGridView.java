/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.unevennesstextgridsample;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class UnevennessTextGridView extends LinearLayout {
    @NonNull
    private final ViewCache<TextView> mTextCache;
    @NonNull
    private final ViewCache<View> mDividerCache;
    @NonNull
    private final List<LinearLayout> mLinearLayouts = new ArrayList<>();
    @NonNull
    private final List<View> mDividers = new ArrayList<>();
    @NonNull
    private final Paint mPaint = new Paint();
    private final float mDividerSize;
    private final float mMargin;
    @Nullable
    private OnTextClickListener mOnTextClickListener;

    public interface OnTextClickListener {
        void onTextClick(@NonNull final String text);
    }

    public UnevennessTextGridView(@NonNull final Context context) {
        this(context, null);
    }

    public UnevennessTextGridView(
            @NonNull final Context context,
            @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UnevennessTextGridView(
            @NonNull final Context context,
            @Nullable final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_unevenness_grid, this);
        mLinearLayouts.add((LinearLayout) findViewById(R.id.linear_layout1));
        mLinearLayouts.add((LinearLayout) findViewById(R.id.linear_layout2));
        mLinearLayouts.add((LinearLayout) findViewById(R.id.linear_layout3));
        mLinearLayouts.add((LinearLayout) findViewById(R.id.linear_layout4));
        mDividers.add((View) findViewById(R.id.divider1));
        mDividers.add((View) findViewById(R.id.divider2));
        mDividers.add((View) findViewById(R.id.divider3));
        final Resources resources = context.getResources();
        mDividerSize = resources.getDimension(R.dimen.divider);
        mPaint.setTextSize(resources.getDimension(R.dimen.font_size));
        mMargin = resources.getDimension(R.dimen.margin) * 2;
        mTextCache = new ViewCache<>(context, c ->
                (TextView) inflater.inflate(R.layout.item, this, false));
        mTextCache.setTerminator(view -> {
            view.setOnClickListener(null);
            view.setText(null);
        });
        mDividerCache = new ViewCache<>(context, c -> {
            final View view = new View(c);
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.divider));
            return view;
        });
        setList(Collections.emptyList());
    }

    public void setOnTextClickListener(@Nullable final OnTextClickListener listener) {
        mOnTextClickListener = listener;
    }

    @NonNull
    private List<List<Pair<Integer, String>>> assignToLine(@NonNull final List<String> list) {
        final List<List<Pair<Integer, String>>> results = new ArrayList<>();
        for (final LinearLayout layout : mLinearLayouts) {
            results.add(new ArrayList<>());
        }

        final int viewWidth = getWidth();
        final Iterator<String> iterator = list.iterator();
        int line = 0;
        int width = 0;
        List<Pair<Integer, String>> textList = results.get(line);
        while (iterator.hasNext()) {
            String text = iterator.next();
            final int itemWidth = (int) (mPaint.measureText(text) + mMargin);
            if (width + itemWidth > viewWidth) {
                line++;
                if (line >= results.size()) {
                    break;
                }
                width = 0;
                textList = results.get(line);
            } else {
                width += itemWidth;
                textList.add(new Pair<>(itemWidth, text));
            }
        }
        return results;
    }

    private void execOnLayoutOnce(@NonNull final Runnable runnable) {
        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                runnable.run();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public void setList(@NonNull final List<String> list) {
        if (getWidth() == 0) {
            execOnLayoutOnce(() -> setList(list));
            return;
        }
        for (LinearLayout layout : mLinearLayouts) {
            layout.removeAllViews();
        }
        mTextCache.recycle();
        mDividerCache.recycle();
        final List<List<Pair<Integer, String>>> assignment = assignToLine(list);

        for (int i = 0; i < mLinearLayouts.size(); i++) {
            final LinearLayout layout = mLinearLayouts.get(i);
            final List<Pair<Integer, String>> line = assignment.get(i);
            if (line.size() == 0) {
                layout.setVisibility(GONE);
                if (i != 0) {
                    mDividers.get(i - 1).setVisibility(GONE);
                }
            } else {
                layout.setVisibility(VISIBLE);
                if (i != 0) {
                    mDividers.get(i - 1).setVisibility(VISIBLE);
                }
            }
            for (final Pair<Integer, String> pair : line) {
                if (layout.getChildCount() != 0) {
                    layout.addView(mDividerCache.obtain(), createDividerLayoutParam());
                }
                final TextView textView = mTextCache.obtain();
                textView.setText(pair.second);
                textView.setOnClickListener(v -> {
                    if (mOnTextClickListener != null) {
                        mOnTextClickListener.onTextClick(pair.second);
                    }
                });
                layout.addView(textView, createTextLayoutParam(pair.first));
            }
        }
        mTextCache.shrink();
        mDividerCache.shrink();
    }

    @NonNull
    private LayoutParams createTextLayoutParam(final int weight) {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, weight);
    }

    @NonNull
    private LayoutParams createDividerLayoutParam() {
        return new LayoutParams((int) mDividerSize, LayoutParams.MATCH_PARENT);
    }
}
