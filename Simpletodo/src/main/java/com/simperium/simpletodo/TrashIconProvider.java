package com.simperium.simpletodo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;

public class TrashIconProvider extends ActionProvider {

    public interface OnClearCompletedListener {
        public void onClearCompleted();
    }

    private WeakReference<OnClearCompletedListener> mListener;

    protected Context mContext;
    protected View mView;
    protected MenuItem mItem;
    protected int mBadgeCount = 0;
    protected TextView mBadge;

    public TrashIconProvider(Context context) {
        super(context);
        mContext = context;
    }

    public void setBadgeCount(int count) {
        mBadgeCount = count;
    }

    public boolean updateBadgeCount(int count) {
        return count != mBadgeCount;
    }

    @Override
    public View onCreateActionView() {
        throw new RuntimeException("Nope");
    }

    @Override
    public View onCreateActionView(MenuItem forItem) {
        mItem = forItem;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.trash_option_item, null);
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onPerformDefaultAction();
            }

        });
        mView = view;
        mBadge = (TextView) view.findViewById(R.id.badge);
        mBadge.setText(String.valueOf(mBadgeCount));
        return view;
    }

    @Override
    public boolean onPerformDefaultAction() {

        OnClearCompletedListener listener = getOnClearCompletedListener();

        if (listener == null)
            return false;

        listener.onClearCompleted();
        return true;
    }

    @Override
    public boolean overridesItemVisibility() {
        return true;
    }

    @Override
    public boolean isVisible() {
        return mBadgeCount > 0;
    }

    public void setOnClearCompletedListener(OnClearCompletedListener listener) {
        mListener = new WeakReference<OnClearCompletedListener>(listener);
    }

    public OnClearCompletedListener getOnClearCompletedListener() {
        if (mListener == null)
            return null;

        return mListener.get();
    }

}