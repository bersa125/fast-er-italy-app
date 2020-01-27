package com.presentation_layer.fasteritaly.activity.tableview;

import android.os.Bundle;
import android.view.Gravity;

import com.presentation_layer.fasteritaly.activity.tableview.model.CellModel;
import com.presentation_layer.fasteritaly.activity.tableview.model.ColumnHeaderModel;
import com.presentation_layer.fasteritaly.activity.tableview.model.RowHeaderModel;

import java.util.ArrayList;
import java.util.List;

public abstract class Map_Results_TableViewModel {

    public static final int VOTE_TYPE = 1;

    private List<ColumnHeaderModel> mColumnHeaderModelList;
    private List<RowHeaderModel> mRowHeaderModelList;
    private List<List<CellModel>> mCellModelList;

    public abstract  int getCellItemViewType(int column);

    public abstract  int getColumnTextAlign(int column);

    public abstract  List<ColumnHeaderModel> getColumHeaderModeList();

    public abstract  List<RowHeaderModel> getRowHeaderModelList();

    public abstract List<List<CellModel>> getCellModelList();

    //Easy model setup
    public abstract void generateListForTableView(List<Bundle> data, List<String> dataAttributes, List<String> HeaderTraslation);
}
