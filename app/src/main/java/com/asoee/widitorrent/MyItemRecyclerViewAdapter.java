package com.asoee.widitorrent;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peak.salut.SalutDevice;

import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<SalutDevice> mValues;
    private final OnListInteractionListener mListener;

    public MyItemRecyclerViewAdapter(List<SalutDevice> items, OnListInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    public void add() {
//        for (SalutDevice device : mValues) {
//            if (device.deviceName.equals(new_owner.deviceName))
//                return;
//        }
//        mValues.add(new_owner);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        ((TextView) holder.mView.findViewById(R.id.group_name))
                .setText(holder.mItem.readableName + "");
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
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public SalutDevice mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;

        }
    }
}
