package com.presentation_layer.fasteritaly.activity.recyclerview_adapter;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fasteritaly.R;
import com.presentation_layer.fasteritaly.activity.SettingsFragment;
import com.presentation_layer.fasteritaly.view.HistoryView;
import com.presentation_layer.fasteritaly.view.SettingsFragmentView;

import java.util.List;

// stores and recycles views as they are scrolled off screen
public class SettingsAddressesRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private SettingsFragmentView view;
        private List<String> objects;

        private TextView text;

        public SettingsAddressesRecyclerViewHolder(View itemView, List<String> objects, SettingsFragmentView listener) {
            super(itemView);
            view=listener;
            this.objects=objects;
            itemView.setOnClickListener(this);
            text= itemView.findViewById(R.id.address_content);
        }

        public TextView getText(){
            return text;
        }

        @Override
        public void onClick(View view) {
            this.view.showAddressModDialog(objects.get(getAdapterPosition()),getAdapterPosition());
        }
}
