package com.presentation_layer.fasteritaly.activity.tableview;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractSorterViewHolder;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.example.fasteritaly.R;
import com.presentation_layer.fasteritaly.activity.tableview.holder.CellViewHolder;
import com.presentation_layer.fasteritaly.activity.tableview.holder.ColumnHeaderViewHolder;
import com.presentation_layer.fasteritaly.activity.tableview.holder.RowHeaderViewHolder;
import com.presentation_layer.fasteritaly.activity.tableview.holder.VoteCellViewHolder;
import com.presentation_layer.fasteritaly.activity.tableview.model.CellModel;
import com.presentation_layer.fasteritaly.activity.tableview.model.ColumnHeaderModel;
import com.presentation_layer.fasteritaly.activity.tableview.model.RowHeaderModel;

import java.util.List;

public class Map_Results_TableAdapter extends AbstractTableAdapter<ColumnHeaderModel, RowHeaderModel,
        CellModel> {

    private Map_Results_TableViewModel mapResultsTableViewModel;

    public Map_Results_TableAdapter(Context p_jContext,boolean type) {
        super(p_jContext);
        if(!type) {
            this.mapResultsTableViewModel = new Map_Results_ER_TableViewModel();
        }else{
            this.mapResultsTableViewModel = new Map_Results_Drug_TableViewModel();
        }
    }

    public Map_Results_TableViewModel getViewModel(){
        return this.mapResultsTableViewModel;
    }
    @Override
    public AbstractViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        View layout;

        switch (viewType) {

            case Map_Results_ER_TableViewModel.VOTE_TYPE:
                // Get gender cell xml Layout
                layout = LayoutInflater.from(mContext).inflate(R.layout
                        .fragment_map_tableview_vote_cell_layout, parent, false);
                return new VoteCellViewHolder(layout);

            default:
                // Get default Cell xml Layout
                layout = LayoutInflater.from(mContext).inflate(R.layout.fragment_map_tableview_cell_layout,
                        parent, false);

                // Create a Cell ViewHolder
                return new CellViewHolder(layout);
        }
    }

    @Override
    public void onBindCellViewHolder(AbstractViewHolder holder, Object p_jValue, int
            p_nXPosition, int p_nYPosition) {
        CellModel cell = (CellModel) p_jValue;

        if (holder instanceof CellViewHolder) {
            // Get the holder to update cell item text
            ((CellViewHolder) holder).setCellModel(cell, p_nXPosition);

        }else if(holder instanceof VoteCellViewHolder){
            ((VoteCellViewHolder) holder).setCellModel(cell);
        }

    }

    @Override
    public AbstractSorterViewHolder onCreateColumnHeaderViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout
                .fragment_map_tableview_column_header_layout, parent, false);
        return new ColumnHeaderViewHolder(layout, getTableView());
    }

    @Override
    public void onBindColumnHeaderViewHolder(AbstractViewHolder holder, Object p_jValue, int p_nXPosition) {
        ColumnHeaderModel columnHeader = (ColumnHeaderModel) p_jValue;

        // Get the holder to update cell item text
        ColumnHeaderViewHolder columnHeaderViewHolder = (ColumnHeaderViewHolder) holder;

        columnHeaderViewHolder.setColumnHeaderModel(columnHeader, p_nXPosition);
    }

    @Override
    public AbstractViewHolder onCreateRowHeaderViewHolder(ViewGroup parent, int viewType) {

        // Get Row Header xml Layout
        View layout = LayoutInflater.from(mContext).inflate(R.layout.fragment_map_tableview_row_header_layout,
                parent, false);

        // Create a Row Header ViewHolder
        return new RowHeaderViewHolder(layout);
    }

    @Override
    public void onBindRowHeaderViewHolder(AbstractViewHolder holder, Object p_jValue, int
            p_nYPosition) {

        RowHeaderModel rowHeaderModel = (RowHeaderModel) p_jValue;

        RowHeaderViewHolder rowHeaderViewHolder = (RowHeaderViewHolder) holder;
        rowHeaderViewHolder.row_header_textview.setText(rowHeaderModel.getData());

    }

    @Override
    public View onCreateCornerView() {
        return LayoutInflater.from(mContext).inflate(R.layout.fragment_map_tableview_corner_layout, null, false);
    }

    @Override
    public int getColumnHeaderItemViewType(int position) {
        return 0;
    }

    @Override
    public int getRowHeaderItemViewType(int position) {
        return 0;
    }

    @Override
    public int getCellItemViewType(int position) {
        return mapResultsTableViewModel.getCellItemViewType(position);
    }

    public void setList(List<Bundle> data, List<String> attributes,List<String> headers) {
        // Generate the lists that are used to TableViewAdapter
        mapResultsTableViewModel.generateListForTableView(data, attributes, headers);

        // Now we got what we need to show on TableView.
        setAllItems(mapResultsTableViewModel.getColumHeaderModeList(), mapResultsTableViewModel
                .getRowHeaderModelList(), mapResultsTableViewModel.getCellModelList());
    }

}
