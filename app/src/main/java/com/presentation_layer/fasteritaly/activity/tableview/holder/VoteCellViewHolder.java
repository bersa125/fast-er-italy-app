package com.presentation_layer.fasteritaly.activity.tableview.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;

import androidx.core.content.ContextCompat;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.example.fasteritaly.R;
import com.presentation_layer.fasteritaly.activity.tableview.model.CellModel;

public class VoteCellViewHolder extends AbstractViewHolder {
    public final RatingBar cell_ratingBar;
    public final LinearLayout cell_container;

    public VoteCellViewHolder(View itemView) {
        super(itemView);
        cell_ratingBar = itemView.findViewById(R.id.ratingBar);
        cell_container = itemView.findViewById(R.id.cell_container);
    }

    public void setCellModel(CellModel p_jModel) {

        // Set text
        cell_ratingBar.setRating(Float.parseFloat((String) p_jModel.getData()));

        // It is necessary to remeasure itself.
        cell_container.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        //cell_ratingBar.requestLayout();
    }

    @Override
    public void setSelected(SelectionState p_nSelectionState) {
        super.setSelected(p_nSelectionState);

       /* if (p_nSelectionState == SelectionState.SELECTED) {
            changeColorOfMoneyTextView(R.color.selected_text_color);
        } else {
            changeColorOfMoneyTextView(R.color.unselected_text_color);
        }*/
        itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(),
                R.color.white));
    }

}
