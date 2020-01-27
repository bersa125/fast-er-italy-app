package com.presentation_layer.fasteritaly.activity;

import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.evrencoskun.tableview.listener.ITableViewListener;
import com.evrencoskun.tableview.sort.SortState;
import com.example.fasteritaly.R;
import com.google.android.gms.maps.model.LatLng;
import com.presentation_layer.fasteritaly.activity.tableview.Map_Results_TableAdapter;
import com.presentation_layer.fasteritaly.activity.tableview.holder.CellViewHolder;
import com.presentation_layer.fasteritaly.activity.tableview.holder.ColumnHeaderViewHolder;
import com.presentation_layer.fasteritaly.activity.tableview.holder.VoteCellViewHolder;
import com.presentation_layer.fasteritaly.activity.tableview.popup.ColumnHeaderLongPressPopup;
import com.presentation_layer.fasteritaly.interactor.SearchOnMapFragmentInteractor;
import com.presentation_layer.fasteritaly.presenter.SearchOnMapFragmentPresenter;
import com.presentation_layer.fasteritaly.view.SearchOnMapFragmentView;
import com.presentation_layer.fasteritaly.view.SearchOnMapView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OnSearchMap_List_Fragment extends Fragment implements SearchOnMapFragmentView , ITableViewListener {

    private View thisView;

    private Map<String,Bundle> resultPoints=new HashMap<>();
    private LatLng centerCoordinates;
    private String centerAddress;

    private ConstraintLayout layout;
    private TableView mTableView;
    private View mProgressBar;
    private View mSmallProgressBar;
    private Map_Results_TableAdapter mTableAdapter;
    private List<Bundle> resultsList=new LinkedList<>();
    //LAT LONG BEST_QUEUE WORST_QUEUE OPEN TYPE + PLACE ADDRESS TOT_QUEUE TIME
    private List<String> BundleAttributes=new LinkedList<>();
    private List<String> HeaderAttributes=new LinkedList<>();

    private SearchOnMapFragmentPresenter presenter;

    public static OnSearchMap_List_Fragment newInstance() {
        OnSearchMap_List_Fragment fragment = new OnSearchMap_List_Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(presenter==null){
            presenter=new SearchOnMapFragmentPresenter(this,new SearchOnMapFragmentInteractor());
        }
    }

    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        thisView = inflater.inflate(R.layout.fragment_search_on_map_list, container, false);
        layout=thisView.findViewById(R.id.constraintLayout);
        mProgressBar = thisView.findViewById(R.id.map_progressBar);
        mSmallProgressBar = thisView.findViewById(R.id.map_progressBarSmall);
        mTableView = thisView.findViewById(R.id.map_table_content_container);

        if(this.getActivity() instanceof SearchOnMapView){
            if(!((SearchOnMapView)this.getActivity()).getType()){
                BundleAttributes.add("PLACE");
                BundleAttributes.add("TOT_WAIT_QUEUE");
                BundleAttributes.add("AVG_VOTE");
                HeaderAttributes.add(getString(R.string.place_name));
                HeaderAttributes.add(getString(R.string.tot_queue));
                HeaderAttributes.add(getString(R.string.avg_vote));
            }else{
                BundleAttributes.add("PLACE");
                BundleAttributes.add("ADDRESS");
                BundleAttributes.add("TIME");
                BundleAttributes.add("OPEN");
                HeaderAttributes.add(getString(R.string.place_name));
                HeaderAttributes.add(getString(R.string.address));
                HeaderAttributes.add(getString(R.string.opening_time));
                HeaderAttributes.add(getString(R.string.now_open));
            }
        }

        //Table initialization

        initializeTableView(mTableView);

        if(presenter==null){
            presenter=new SearchOnMapFragmentPresenter(this,new SearchOnMapFragmentInteractor());
        }

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onSetSelectedItem(null);
            }
        });

        return thisView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        BundleAttributes.clear();
        HeaderAttributes.clear();
        presenter.onDetach();
    }

    @Override
    public void clearResults() {
        try {
            resultPoints.clear();
            resultsList.clear();
            mTableAdapter.setList(resultsList, BundleAttributes, HeaderAttributes);
            mTableView.setVisibility(View.INVISIBLE);
        }catch (Exception e){}
    }

    @Override
    public void addResult(Bundle data) {
        resultPoints.put(data.getString("ADDRESS"),data);
        resultsList.clear();
        for(Bundle b: resultPoints.values()){
            if(b.getBoolean("TYPE")){//Adds further info for the table representation as String
                b.putString("AFFERMATIVE",getResources().getString(R.string.Yes));
                b.putString("NEGATIVE",getResources().getString(R.string.No));
            }
            resultsList.add(b);
        }
        mTableAdapter.setList(resultsList,BundleAttributes,HeaderAttributes);
        mTableView.setVisibility(View.VISIBLE);

    }

    @Override
    public void addResults(List<Bundle> data) {
        resultsList.clear();
        for(Bundle b:data){
            resultPoints.put(b.getString("ADDRESS"),b);
            if(b.getBoolean("TYPE")){//Adds further info for the table represantation as String
                b.putString("AFFERMATIVE",getResources().getString(R.string.Yes));
                b.putString("NEGATIVE",getResources().getString(R.string.No));
            }
            resultsList.add(b);
        }
        mTableAdapter.setList(resultsList,BundleAttributes,HeaderAttributes);
        mTableView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setCenterPosition(String address) {
        presenter.onCentralLocationSearchRequest(address);
    }

    @Override
    public void setCenterPosition(Location coordinates) {
        presenter.onCentralLocationSearchRequest(coordinates);
    }

    @Override
    public void setCenterPosition(LatLng coordinates, String address) {
        centerCoordinates = coordinates;
        centerAddress = address;
        presenter.onCentralLocationSet();
    }

    @Override
    public void onItemSelected(Bundle object) {
        //Finds the row and bolds it
        if(resultsList.size()>0) {
            int yRow = -1;
            if(object!=null) {
                for (int j = 0; j < resultsList.size(); j++) {
                    boolean found = false;
                    for (int i = 0; i < BundleAttributes.size(); i++) {
                        found = true;
                        if (mTableView.getCellLayoutManager().getCellViewHolder(i, j) instanceof CellViewHolder) {
                            CellViewHolder cell = (CellViewHolder) mTableView.getCellLayoutManager().getCellViewHolder(i, j);
                            if (object.get(BundleAttributes.get(i)) instanceof Boolean) {
                                if ((object.getBoolean(BundleAttributes.get(i)) && cell.cell_textview.getText().equals(getString(R.string.No))) || (!object.getBoolean(BundleAttributes.get(i)) && cell.cell_textview.getText().equals(getString(R.string.Yes)))) {
                                    found = false;
                                    break;
                                }
                            } else {
                                if (!object.get(BundleAttributes.get(i)).toString().equals(cell.cell_textview.getText())) {
                                    found = false;
                                    break;
                                }
                            }
                        } else if (mTableView.getCellLayoutManager().getCellViewHolder(i, j) instanceof VoteCellViewHolder) {
                            VoteCellViewHolder cell = (VoteCellViewHolder) mTableView.getCellLayoutManager().getCellViewHolder(i, j);
                            if (object.getInt(BundleAttributes.get(i)) != (int) cell.cell_ratingBar.getRating()) {
                                found = false;
                                break;
                            }
                        }
                    }
                    if (found) {
                        yRow = j;
                        break;
                    }
                }
            }
            if (yRow != -1) {
                for (int i = 0; i < BundleAttributes.size(); i++) {
                    for (int j = 0; j < resultsList.size(); j++) {
                        if ((mTableView.getCellLayoutManager().getCellViewHolder(i, j)) instanceof CellViewHolder) {
                            if (j == yRow) {
                                ((CellViewHolder) mTableView.getCellLayoutManager().getCellViewHolder(i, j)).setSelected(AbstractViewHolder.SelectionState.SELECTED);
                            } else {
                                ((CellViewHolder) mTableView.getCellLayoutManager().getCellViewHolder(i, j)).setSelected(AbstractViewHolder.SelectionState.UNSELECTED);
                            }
                        }
                    }
                    mTableView.remeasureColumnWidth(i);
                }
            }else{
                for (int i = 0; i < BundleAttributes.size(); i++) {
                    for (int j = 0; j < resultsList.size(); j++) {
                        if ((mTableView.getCellLayoutManager().getCellViewHolder(i, j)) instanceof CellViewHolder) {
                            ((CellViewHolder) mTableView.getCellLayoutManager().getCellViewHolder(i, j)).setSelected(AbstractViewHolder.SelectionState.UNSELECTED);
                        }
                    }
                    mTableView.remeasureColumnWidth(i);
                }
            }
        }
    }

    @Override
    public void showLoadingBar(boolean status,boolean small) {
        if(status){
            showProgressBar(small);
        }else{
            hideProgressBar(small);
        }
    }

    @Override
    public LatLng getCenterCoordinates() {
        return centerCoordinates;
    }

    @Override
    public String getCenterAddress() {
        return centerAddress;
    }

    @Override
    public Object getAttachedView() {
        return this.getActivity();
    }

    private void showProgressBar(boolean small) {
        if(!small)
            mProgressBar.setVisibility(View.VISIBLE);
        else
            mSmallProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(boolean small) {
        if(!small)
            mProgressBar.setVisibility(View.GONE);
        else
            mSmallProgressBar.setVisibility(View.GONE);
    }

    private void initializeTableView(TableView tableView){
        // Create TableView Adapter
        mTableAdapter = new Map_Results_TableAdapter(getContext(),((SearchOnMapView)this.getActivity()).getType());
        tableView.setAdapter(mTableAdapter);
        // Create listener
        tableView.setTableViewListener(this);
    }

    //Table listeners
    @Override
    public void onCellClicked(@NonNull RecyclerView.ViewHolder viewHolder, int i, /*This is the row*/int i1) {
        //solves the correct Bundle
        if (!resultsList.isEmpty()) {
            presenter.onSetSelectedItem(resultsList.get(i1));
            presenter.onSetSelectedItem(resultsList.get(i1));
        }
    }
    @Override
    public void onCellLongPressed(@NonNull RecyclerView.ViewHolder viewHolder, int i, int i1) {
        if(!resultsList.isEmpty()) {
            presenter.onSelectedItem(resultsList.get(i1));
        }
    }
    @Override
    public void onColumnHeaderClicked(@NonNull final RecyclerView.ViewHolder columnHeaderView, int column) {
        if (columnHeaderView != null && columnHeaderView instanceof ColumnHeaderViewHolder) {
            //Sorting on click
            try {
                for (int i = 0; i < BundleAttributes.size(); i++) {
                    if (i != columnHeaderView.getAdapterPosition())
                        mTableView.sortColumn(i, SortState.UNSORTED);
                }
                SortState sortState = mTableView.getSortingStatus(columnHeaderView.getAdapterPosition());
                if (sortState == SortState.UNSORTED) {
                    mTableView.sortColumn(columnHeaderView.getAdapterPosition(), SortState.DESCENDING);
                } else if (sortState == SortState.DESCENDING) {
                    mTableView.sortColumn(columnHeaderView.getAdapterPosition(), SortState.ASCENDING);
                } else if (sortState == SortState.ASCENDING) {
                    mTableView.sortColumn(columnHeaderView.getAdapterPosition(), SortState.DESCENDING);
                }
                // Recalculate of the width values of the columns
                mTableView.remeasureColumnWidth(columnHeaderView.getAdapterPosition());
                //Remove possible Bold columns
                for (int i = 0; i < BundleAttributes.size(); i++) {
                    for (int j = 0; j < resultsList.size(); j++) {
                        if ((mTableView.getCellLayoutManager().getCellViewHolder(i, j)) instanceof CellViewHolder) {
                            ((CellViewHolder) mTableView.getCellLayoutManager().getCellViewHolder(i, j)).setSelected(AbstractViewHolder.SelectionState.UNSELECTED);
                        }
                    }
                    mTableView.remeasureColumnWidth(i);
                }
            }catch(Exception e){}
            presenter.onSetSelectedItem(null);
        }

    }
    @Override
    public void onColumnHeaderLongPressed(@NonNull RecyclerView.ViewHolder columnHeaderView, int
            column) {
        if (columnHeaderView != null && columnHeaderView instanceof ColumnHeaderViewHolder) {
            // Create Long Press Popup
            ColumnHeaderLongPressPopup popup = new ColumnHeaderLongPressPopup(
                    (ColumnHeaderViewHolder) columnHeaderView, mTableView,BundleAttributes.size());
            // Show
            popup.show();
        }
    }
    @Override
    public void onRowHeaderClicked(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        //Remove possible Bold columns
        for (int k = 0; k < BundleAttributes.size(); k++) {
            for (int j = 0; j < resultsList.size(); j++) {
                if ((mTableView.getCellLayoutManager().getCellViewHolder(k, j)) instanceof CellViewHolder) {
                    ((CellViewHolder) mTableView.getCellLayoutManager().getCellViewHolder(k, j)).setSelected(AbstractViewHolder.SelectionState.UNSELECTED);
                }
            }
            mTableView.remeasureColumnWidth(k);
        }
        presenter.onSetSelectedItem(null);
    }
    @Override
    public void onRowHeaderLongPressed(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

}