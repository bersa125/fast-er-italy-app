package com.presentation_layer.fasteritaly.activity.tableview;

import android.os.Bundle;
import android.view.Gravity;

import com.presentation_layer.fasteritaly.activity.tableview.model.CellModel;
import com.presentation_layer.fasteritaly.activity.tableview.model.ColumnHeaderModel;
import com.presentation_layer.fasteritaly.activity.tableview.model.RowHeaderModel;

import java.util.ArrayList;
import java.util.List;


public class Map_Results_Drug_TableViewModel extends Map_Results_TableViewModel {
    private List<ColumnHeaderModel> mColumnHeaderModelList;
    private List<RowHeaderModel> mRowHeaderModelList;
    private List<List<CellModel>> mCellModelList;

    public int getCellItemViewType(int column) {

        switch (column) {
            default:
                return 0;//String
        }
    }

    public int getColumnTextAlign(int column) {
        switch (column) {
            default:
                return Gravity.CENTER;
        }

    }

    private List<ColumnHeaderModel> createColumnHeaderModelList(List<String> columnHeaders) {
        List<ColumnHeaderModel> list = new ArrayList<>();

        for(String header: columnHeaders){
            list.add(new ColumnHeaderModel(header));
        }

        return list;
    }

    private List<List<CellModel>> createCellModelList(List<Bundle> data,List<String> columnHeaders ) {
        List<List<CellModel>> lists = new ArrayList<>();
        int i=0;
        for(Bundle singleDat :data){
            List<CellModel> list = new ArrayList<>();
            int j=0;
            for(String header:columnHeaders){
                if(header.equals("OPEN")){
                    list.add(new CellModel(j + "-" + i, singleDat.getBoolean(header)?singleDat.getString("AFFERMATIVE"):singleDat.getString("NEGATIVE")));
                }else {
                    list.add(new CellModel(j + "-" + i, singleDat.get(header).toString()));
                }
                j++;
            }
            i++;
            lists.add(list);
        }
        return lists;
    }

    private List<RowHeaderModel> createRowHeaderList(int size) {
        List<RowHeaderModel> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new RowHeaderModel(String.valueOf(i + 1)));
        }
        return list;
    }


    public List<ColumnHeaderModel> getColumHeaderModeList() {
        return mColumnHeaderModelList;
    }

    public List<RowHeaderModel> getRowHeaderModelList() {
        return mRowHeaderModelList;
    }

    public List<List<CellModel>> getCellModelList() {
        return mCellModelList;
    }

    //Easy model setup
    public void generateListForTableView(List<Bundle> data,List<String> dataAttributes,List<String> HeaderTraslation) {
        mColumnHeaderModelList = createColumnHeaderModelList(HeaderTraslation);
        mCellModelList = createCellModelList(data,dataAttributes);
        mRowHeaderModelList = createRowHeaderList(data.size());
    }

}



