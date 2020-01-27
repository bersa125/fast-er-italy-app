package com.presentation_layer.fasteritaly.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.fasteritaly.R;
import com.presentation_layer.fasteritaly.activity.loading_dialog.Loading_Dialog;
import com.presentation_layer.fasteritaly.interactor.PlaceInfoFragmentInteractor;
import com.presentation_layer.fasteritaly.presenter.PlaceInfoFragmentPresenter;
import com.presentation_layer.fasteritaly.view.PlaceInfoFragmentView;


public class PlaceInfoFragment extends Fragment implements PlaceInfoFragmentView {

    private View thisView;
    private PlaceInfoFragmentPresenter presenter;

    public PlaceInfoFragment() {
        // Required empty public constructor
    }

    public static PlaceInfoFragment newInstance(Bundle object) {
        PlaceInfoFragment fragment = new PlaceInfoFragment();
        fragment.setArguments(object);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(presenter==null){
            presenter=new PlaceInfoFragmentPresenter(this,new PlaceInfoFragmentInteractor());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        thisView=inflater.inflate(R.layout.fragment_details_address, container, false);//default
        if(presenter==null){
            presenter=new PlaceInfoFragmentPresenter(this,new PlaceInfoFragmentInteractor());
        }
        presenter.onPopulate(getArguments().getInt("FRAGMENT_TYPE"),getArguments(),inflater,container);
        return thisView;
    }

    @Override
    public void setCorrectLayout(int type,LayoutInflater inflater, ViewGroup container) {
        switch (type){
            case PlaceInfoFragmentView.TYPE_NAME_ADDRESS:
                //setup the correct layout
                thisView=inflater.inflate(R.layout.fragment_details_address,container, false);
                break;
            case PlaceInfoFragmentView.TYPE_STATE_ER:
                thisView=inflater.inflate(R.layout.fragment_details_queue,container, false);
                break;
            case PlaceInfoFragmentView.TYPE_ER_GENERAL_VOTES:
                thisView=inflater.inflate(R.layout.fragment_details_votes,container, false);
                break;
            case PlaceInfoFragmentView.TYPE_DRUG_OPENING_TIME:
                thisView=inflater.inflate(R.layout.fragment_details_open_time,container, false);
                break;
        }
    }

    @Override
    public void updateFragment(int type, final Bundle newData) {
        switch (type){
            case PlaceInfoFragmentView.TYPE_NAME_ADDRESS:
                if(getArguments().getInt("FRAGMENT_TYPE")==PlaceInfoFragmentView.TYPE_NAME_ADDRESS) {
                    //setup the correct content of the layout
                    ((TextView) thisView.findViewById(R.id.place_name)).setText(newData.get("PLACE").toString());
                    ((TextView) thisView.findViewById(R.id.address_of_place)).setText(newData.get("ADDRESS").toString());
                }
                break;
            case PlaceInfoFragmentView.TYPE_STATE_ER:
                Loading_Dialog.getInstance().dismissDialog();
                if(getArguments().getInt("FRAGMENT_TYPE")==PlaceInfoFragmentView.TYPE_STATE_ER) {
                    //First Grid
                    ((TextView) thisView.findViewById(R.id.wait_red)).setText((newData.get("RED_WAIT_QUEUE") != null) ? ("\u25CF  " + newData.get("RED_WAIT_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.wait_yellow)).setText((newData.get("YELLOW_WAIT_QUEUE") != null) ? ("\u25CF  " + newData.get("YELLOW_WAIT_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.wait_green)).setText((newData.get("GREEN_WAIT_QUEUE") != null) ? ("\u25CF  " + newData.get("GREEN_WAIT_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.wait_white)).setText((newData.get("WHITE_WAIT_QUEUE") != null) ? ("\u25CF  " + newData.get("WHITE_WAIT_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.wait_not_exec)).setText((newData.get("NO_EXEC_WAIT_QUEUE") != null) ? ("\u25CF  " + newData.get("NO_EXEC_WAIT_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.wait_tot)).setText((newData.get("TOT_WAIT_QUEUE") != null) ? ("\u25CF  " + newData.get("TOT_WAIT_QUEUE").toString()) : "-N/A-");
                    //Second Grid
                    ((TextView) thisView.findViewById(R.id.treatment_red)).setText((newData.get("RED_TREAT_QUEUE") != null) ? ("\u25CF  " + newData.get("RED_TREAT_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.treatment_yellow)).setText((newData.get("YELLOW_TREAT_QUEUE") != null) ? ("\u25CF  " + newData.get("YELLOW_TREAT_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.treatment_green)).setText((newData.get("GREEN_TREAT_QUEUE") != null) ? ("\u25CF  " + newData.get("GREEN_TREAT_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.treatment_white)).setText((newData.get("WHITE_TREAT_QUEUE") != null) ? ("\u25CF  " + newData.get("WHITE_TREAT_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.treatment_not_exec)).setText((newData.get("NO_EXEC_TREAT_QUEUE") != null) ? ("\u25CF  " + newData.get("NO_EXEC_TREAT_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.treatment_tot)).setText((newData.get("TOT_TREAT_QUEUE") != null) ? ("\u25CF  " + newData.get("TOT_TREAT_QUEUE").toString()) : "-N/A-");
                    //Third Grid
                    ((TextView) thisView.findViewById(R.id.observation_red)).setText((newData.get("RED_OBS_QUEUE") != null) ? ("\u25CF  " + newData.get("RED_OBS_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.observation_yellow)).setText((newData.get("YELLOW_OBS_QUEUE") != null) ? ("\u25CF  " + newData.get("YELLOW_OBS_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.observation_green)).setText((newData.get("GREEN_OBS_QUEUE") != null) ? ("\u25CF  " + newData.get("GREEN_OBS_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.observation_white)).setText((newData.get("WHITE_OBS_QUEUE") != null) ? ("\u25CF  " + newData.get("WHITE_OBS_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.observation_not_exec)).setText((newData.get("NO_EXEC_OBS_QUEUE") != null) ? ("\u25CF  " + newData.get("NO_EXEC_OBS_QUEUE").toString()) : "-N/A-");
                    ((TextView) thisView.findViewById(R.id.observation_tot)).setText((newData.get("TOT_OBS_QUEUE") != null) ? ("\u25CF  " + newData.get("TOT_OBS_QUEUE").toString()) : "-N/A-");
                    //Write on the update and set up the update button
                    ((TextView) thisView.findViewById(R.id.update_date)).setText((newData.get("UPDATE_DATE") != null) ? (newData.get("UPDATE_DATE").toString()) : "-N/A-");
                    thisView.findViewById(R.id.update_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle request = new Bundle(newData);
                            request.putInt("REQUESTER", PlaceInfoFragmentView.TYPE_STATE_ER);
                            presenter.onActvityUpdateRequest(request);
                        }
                    });
                }
                break;
            case PlaceInfoFragmentView.TYPE_ER_GENERAL_VOTES:
                Loading_Dialog.getInstance().dismissDialog();
                if(getArguments().getInt("FRAGMENT_TYPE")==PlaceInfoFragmentView.TYPE_ER_GENERAL_VOTES) {
                    ((RatingBar) thisView.findViewById(R.id.ratingBar_wait)).setRating(newData.get("AVG_VOTE_WAIT")!=null?newData.getInt("AVG_VOTE_WAIT"):0);
                    ((RatingBar) thisView.findViewById(R.id.ratingBar_structure)).setRating(newData.get("AVG_VOTE_STRUCT")!=null?newData.getInt("AVG_VOTE_STRUCT"):0);
                    ((RatingBar) thisView.findViewById(R.id.ratingBar_service)).setRating(newData.get("AVG_VOTE_SERVICE")!=null?newData.getInt("AVG_VOTE_SERVICE"):0);
                    thisView.findViewById(R.id.update_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle request = new Bundle(newData);
                            request.putInt("REQUESTER", PlaceInfoFragmentView.TYPE_ER_GENERAL_VOTES);
                            presenter.onActvityUpdateRequest(request);
                        }
                    });
                }
                break;
            case PlaceInfoFragmentView.TYPE_DRUG_OPENING_TIME:
                if(getArguments().getInt("FRAGMENT_TYPE")==PlaceInfoFragmentView.TYPE_DRUG_OPENING_TIME) {
                    ((TextView) thisView.findViewById(R.id.opening_time)).setText(newData.get("TIME")!=null?newData.get("TIME").toString():"-N/A-");
                    ((TextView) thisView.findViewById(R.id.open_state)).setText(newData.get("TIME")!=null?(newData.getBoolean("OPEN")?getString(R.string.open):getString(R.string.closed)):"");
                    if(newData.get("TIME")!=null){
                        if(newData.getBoolean("OPEN")){
                            ((TextView) thisView.findViewById(R.id.open_state)).setTextColor(getResources().getColor(R.color.green));
                        }else{
                            ((TextView) thisView.findViewById(R.id.open_state)).setTextColor(getResources().getColor(R.color.red));
                        }
                    }
                }
                break;
        }
    }

    @Override
    public int getType() {
        return getArguments().getInt("FRAGMENT_TYPE");
    }

}
