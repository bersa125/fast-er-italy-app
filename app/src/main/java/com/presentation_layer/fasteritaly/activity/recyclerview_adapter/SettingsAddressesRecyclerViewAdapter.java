package com.presentation_layer.fasteritaly.activity.recyclerview_adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fasteritaly.R;
import com.presentation_layer.fasteritaly.activity.SettingsFragment;
import com.presentation_layer.fasteritaly.view.HistoryView;
import com.presentation_layer.fasteritaly.view.SettingsFragmentView;

import java.util.List;

public class SettingsAddressesRecyclerViewAdapter extends RecyclerView.Adapter<SettingsAddressesRecyclerViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;
    private SettingsFragmentView view;

    // data is passed into the constructor
    public SettingsAddressesRecyclerViewAdapter(Context context, List<String> data, SettingsFragmentView p) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.view=p;
    }

    // inflates the row layout from xml when needed
    @Override
    public SettingsAddressesRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.settings_addresses_row_adapter_layout, parent, false);
        return new SettingsAddressesRecyclerViewHolder(view,mData,this.view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull SettingsAddressesRecyclerViewHolder holder, int position) {
        holder.getText().setText(mData.get(position));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }
}
