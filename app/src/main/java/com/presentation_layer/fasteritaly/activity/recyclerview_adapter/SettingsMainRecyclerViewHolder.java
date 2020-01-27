package com.presentation_layer.fasteritaly.activity.recyclerview_adapter;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fasteritaly.R;
import com.presentation_layer.fasteritaly.view.SettingsFragmentView;

import java.util.List;

// stores and recycles views as they are scrolled off screen
public class SettingsMainRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private SettingsFragmentView view;
        private List<Bundle> objects;

        private ImageView image;
        private TextView text;

        public SettingsMainRecyclerViewHolder(View itemView, List<Bundle> objects, SettingsFragmentView listener) { //BUNDLE IMAGE, TEXT, VALUE
            super(itemView);
            view=listener;
            this.objects=objects;
            itemView.setOnClickListener(this);
            image= itemView.findViewById(R.id.icon_left);
            text= itemView.findViewById(R.id.menu_name);
        }

        public TextView getText(){
            return text;
        }
        public ImageView getImage(){
            return image;
        }

        @Override
        public void onClick(View view) {
            this.view.changeContent(objects.get(getAdapterPosition()).getInt("VALUE"));
        }
}
