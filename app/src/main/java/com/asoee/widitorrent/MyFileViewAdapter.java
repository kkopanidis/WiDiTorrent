package com.asoee.widitorrent;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asoee.widitorrent.data.File;
import com.asoee.widitorrent.data.RequestList;
import com.peak.salut.SalutDevice;

import java.util.List;

public class MyFileViewAdapter extends RecyclerView.Adapter<MyFileViewAdapter.ViewHolder> {

    private final RequestList mValues;

    private final OnListInteractionListener mListener;

    public MyFileViewAdapter(List<File> items, OnListInteractionListener listener) {
        mValues = new RequestList();
        mValues.fileList = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.fileList.get(position);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.fileList.size();
    }

    public void refreshList(File f) {
        mValues.fileList.add(f);
        notifyDataSetChanged();

    }

    public RequestList getList() {
        return mValues;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public Object mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
        }
    }
}
