package com.presentation_layer.fasteritaly.activity.array_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Filter;

import com.example.fasteritaly.R;

import java.util.ArrayList;
import java.util.List;

public class ResultsAdapter extends ArrayAdapter<String> {
    private LayoutInflater layoutInflater;
    List<String> list;

    private Filter mFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            return resultValue.toString();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null) {
                ArrayList<String> suggestions = new ArrayList<>();
                for (String s : list) {
                    // Note: change the "contains" to "startsWith" if you only want starting matches
                    if (s.contains(constraint.toString().toLowerCase())) {
                        suggestions.add(s);
                    }
                }
                results.values = suggestions;
                results.count = suggestions.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results != null && results.count > 0) {
                // we have filtered results
                addAll((ArrayList<String>) results.values);
            } else {
                // no filter, add entire original list back in
                addAll(list);
            }
            notifyDataSetChanged();
        }
    };

    public ResultsAdapter(Context context, int textViewResourceId, List<String> customers) {
        super(context, textViewResourceId, customers);
        // copy all the customers into a master list
        list = new ArrayList<String>();
        list.addAll(customers);
        while(list.size()>3){
            list.remove(list.size()-1);
        }
        layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.suggestion_dropdown_item_layout, null);
        }

        String res = getItem(position);

        CheckedTextView name = view.findViewById(R.id.text1);
        name.setText(res);

        return view;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }
}
