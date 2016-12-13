package com.asoee.widitorrent;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.asoee.widitorrent.data.ConnectionSpeed;
import com.peak.salut.SalutDevice;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private final List<SalutDevice> mValues;
    private final List<ConnectionSpeed> mValues2;

    private final OnListInteractionListener mListener;

    public DeviceAdapter(List<SalutDevice> items, OnListInteractionListener listener) {
        if (items == null)
            mValues = new ArrayList<>();
        else
            mValues = items;
        mValues2 = new ArrayList<>();
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
        holder.mItem = mValues.get(position);
        ((TextView) holder.mView.findViewById(R.id.group_name))
                .setText(mValues.get(position).readableName + "");
        ((TextView) holder.mView.findViewById(R.id.textView2))
                .setText(mValues2.get(position).speed + " Mbps");
        holder.mView.findViewById(R.id.checkBox).setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void refreshList(List<SalutDevice> devices, List<ConnectionSpeed> speeds) {
        mValues2.clear();
        mValues.clear();
        mValues.addAll(devices);
        mValues2.addAll(speeds);

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
