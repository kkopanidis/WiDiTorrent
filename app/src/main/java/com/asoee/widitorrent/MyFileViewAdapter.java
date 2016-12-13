package com.asoee.widitorrent;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.asoee.widitorrent.data.File;
import com.asoee.widitorrent.data.RequestList;

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
        ((TextView) holder.mView.findViewById(R.id.group_name)).setText(((File) holder.mItem).url);
        ((TextView) holder.mView.findViewById(R.id.textView2))
                .setText(((File) holder.mItem).downloaders.size() + "");
        ((CheckBox) holder.mView.findViewById(R.id.checkBox))
                .setChecked(((File) holder.mItem).downloaders.contains(MainActivity.network.thisDevice.readableName));
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
        if (!mValues.fileList.contains(f)) {
            mValues.fileList.add(f);
        } else {
            File f2 = mValues.fileList.get(mValues.fileList.indexOf(f));
            f2.downloaders.addAll(f.downloaders);
        }

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
