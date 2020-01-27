package com.presentation_layer.fasteritaly.activity.recyclerview_adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fasteritaly.R;
import com.presentation_layer.fasteritaly.view.SettingsFragmentView;

import java.util.List;

public class SettingsMainRecyclerViewAdapter extends RecyclerView.Adapter<SettingsMainRecyclerViewHolder> {

    private List<Bundle> mData;
    private LayoutInflater mInflater;
    private SettingsFragmentView view;

    // data is passed into the constructor
    public SettingsMainRecyclerViewAdapter(Context context, List<Bundle> data, SettingsFragmentView p) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.view=p;
    }

    // inflates the row layout from xml when needed
    @Override
    public SettingsMainRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.settings_main_row_adapter_layout, parent, false);
        return new SettingsMainRecyclerViewHolder(view,mData,this.view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull SettingsMainRecyclerViewHolder holder, int position) {
        //Setup dei componenti
        Bundle item=mData.get(position);
        holder.getImage().setImageBitmap((Bitmap) item.getParcelable("IMAGE"));
        holder.getText().setText(item.getString("TEXT"));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // convenience method for getting data at click position
    Bundle getItem(int id) {
        return mData.get(id);
    }
}
