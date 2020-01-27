package com.presentation_layer.fasteritaly.activity.recyclerview_adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fasteritaly.R;
import com.model.fasteritaly.Hospital;
import com.presentation_layer.fasteritaly.presenter.HistoryPresenter;
import com.presentation_layer.fasteritaly.view.HistoryView;

import java.util.List;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewHolder> {

    private List<Bundle> mData;
    private LayoutInflater mInflater;
    private HistoryView view;
    private Context context;

    // data is passed into the constructor
    public HistoryRecyclerViewAdapter(Context context, List<Bundle> data, HistoryView p) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.view=p;
        this.context=context;
    }

    // inflates the row layout from xml when needed
    @Override
    public HistoryRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.history_row_adapter_layout, parent, false);
        return new HistoryRecyclerViewHolder(view,mData,this.view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull HistoryRecyclerViewHolder holder, int position) {
       Bundle hospital = mData.get(position);
       if(hospital.get("PLACE")!=null){
            if(context.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
                holder.getName().setText(this.reduceName(hospital.get("PLACE").toString()));
            }else{
                holder.getName().setText((hospital.get("PLACE").toString()));
            }
       }else{
           holder.getName().setText("Error");
       }
       holder.getDate().setText((hospital.get("DATE") != null) ? (hospital.get("DATE").toString().split("T")[0]) : "Error") ;
       holder.getAvg_vote().setRating((hospital.get("AVG_VOTE_USR") != null) ? (hospital.getInt("AVG_VOTE_USR")) : 0);
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

    private String reduceName(String name){
        String res="";
        String[] words=name.split(" ");
        for(int i=0;i<words.length;i++){
            if(i==0){
                res=words[i].charAt(0)+".";
            }else{
                if((words[i].charAt(0)+"").equals("\"")){
                    if((words[i].charAt(words[i].length()-1)+"").equals("\"")){
                        res = res + words[i].charAt(1)+ ".";
                    }else{
                        res = res + words[i].charAt(0) + words[i].charAt(1)+ ".";
                    }
                }else {
                    if((words[i].charAt(words[i].length()-1)+"").equals("\"")){
                        res = res + words[i].charAt(0)+"."+words[i].charAt(words[i].length()-1);
                    }else{
                        res = res + words[i].charAt(0)+ ".";
                    }
                }
            }
        }
        if(res.length()>6)
            return reduceName(res);
        else
            return res;
    }
}
