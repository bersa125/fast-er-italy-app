package com.presentation_layer.fasteritaly.activity.recyclerview_adapter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fasteritaly.R;
import com.presentation_layer.fasteritaly.presenter.HistoryPresenter;
import com.presentation_layer.fasteritaly.view.HistoryView;

import java.util.List;

// stores and recycles views as they are scrolled off screen
public class HistoryRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private HistoryView view;
        private List<Bundle> objects;

        private TextView name;
        private TextView date;
        private RatingBar avg_vote;

        public HistoryRecyclerViewHolder(View itemView, List<Bundle> objects, HistoryView listener) {
            super(itemView);
            view=listener;
            this.objects=objects;
            itemView.setOnClickListener(this);
            name= itemView.findViewById(R.id.hospital_name);
            date= itemView.findViewById(R.id.date_of_immission);
            avg_vote= itemView.findViewById(R.id.ratingBar);
        }

        public TextView getName(){
            return name;
        }
        public TextView getDate(){
            return date;
        }
        public RatingBar getAvg_vote(){
            return avg_vote;
        }

        @Override
        public void onClick(View view) {
            this.view.showModificationsDialog(objects.get(getAdapterPosition()));
        }
}
