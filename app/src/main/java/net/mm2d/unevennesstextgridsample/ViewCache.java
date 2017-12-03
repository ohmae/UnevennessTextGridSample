/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.unevennesstextgridsample;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class ViewCache<T extends View> {
    private int mUsed;
    private final List<T> mCache = new ArrayList<>();
    private final Context mContext;
    private final Creator<T> mCreator;
    private Terminator<T> mTerminator;

    public interface Creator<T extends View> {
        @NonNull
        T create(@NonNull Context context);
    }

    public interface Terminator<T extends View> {
        void terminate(@NonNull T view);
    }

    ViewCache(@NonNull final Context context, @NonNull final Creator<T> creator) {
        mContext = context;
        mCreator = creator;
    }

    void setTerminator(@Nullable final Terminator<T> terminator) {
        mTerminator = terminator;
    }

    T obtain() {
        if (mUsed < mCache.size()) {
            return mCache.get(mUsed++);
        }
        mUsed++;
        T view = mCreator.create(mContext);
        mCache.add(view);
        return view;
    }

    void shrink() {
        for (int i = mCache.size() - 1; i > mUsed; i--) {
            T view = mCache.remove(i);
            if (mTerminator != null) {
                mTerminator.terminate(view);
            }
        }
    }

    void recycle() {
        mUsed = 0;
    }
}
