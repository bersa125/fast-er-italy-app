package com.presentation_layer.fasteritaly.presenter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.presentation_layer.fasteritaly.interactor.PlaceInfoFragmentInteractor;
import com.presentation_layer.fasteritaly.view.PlaceInfoFragmentView;
import com.presentation_layer.fasteritaly.view.PlaceInfoView;

public class PlaceInfoFragmentPresenter implements PlaceInfoFragmentInteractor.onPlaceInfoFragmentInteractionListener {

    private PlaceInfoFragmentView view;
    private PlaceInfoFragmentInteractor interactor;

    public PlaceInfoFragmentPresenter(PlaceInfoFragmentView v,PlaceInfoFragmentInteractor i){
        view=v;
        interactor=i;
    }

    @Override
    public void onPopulate(int i, Bundle object, LayoutInflater inflater, ViewGroup container) {
        view.setCorrectLayout(i,inflater,container);
        view.updateFragment(i,object);
    }

    @Override
    public void onUpdateData(Bundle object) {
        view.updateFragment(view.getType(),object);
    }

    @Override
    public void onVoteSubmissionRequest() {
        if(view.getActivity() instanceof PlaceInfoView){
            ((PlaceInfoView)view.getActivity()).showVoteInsertionDialog();
        }
    }

    @Override
    public void onActvityUpdateRequest(Bundle object) {
        if(view.getActivity() instanceof PlaceInfoView){
            ((PlaceInfoView)view.getActivity()).requestUpdate(object);
        }
    }

    @Override
    public Object getViewContext() {
        return view.getActivity();
    }
}
