package com.presentation_layer.fasteritaly.presenter;

import android.content.Context;
import android.os.Bundle;

import com.presentation_layer.fasteritaly.interactor.HistoryInteractor;
import com.presentation_layer.fasteritaly.view.HistoryView;

import java.util.List;

public class HistoryPresenter implements HistoryInteractor.onHistoryActivityInteractionListener {

    private HistoryView view;
    private HistoryInteractor interactor;

    public HistoryPresenter(HistoryView v,HistoryInteractor i){
        view=v;
        interactor=i;
    }

    public void onResume() {
        view.checkInternetFunctionality();
        onHistoryPopulate();
    }
    public void onPause() {
        view.clearResults();
        view.stopcheckInternetFunctionality();
    }
    public void onDestroy() {
        view.clearResults();
        view.stopcheckInternetFunctionality();
    }

    @Override
    public void onHistoryPopulate() {//the list is created inside the view and it's attached to a recyclerview
        view.startLoadingScreen();
        interactor.updateVotes(this,view.getItemArrayList());
    }

    @Override
    public void onHistoryUpdate(List<Bundle> bundle) {
        view.populateHistoryView(bundle);
        view.stopLoadingScreen();
    }

    @Override
    public void onTupleSelection(Bundle selected) {
        view.showModificationsDialog(selected);
    }
    @Override
    public void onDeleteVote(Bundle selected) {
        interactor.deleteVote(this,selected);
    }
    @Override
    public void onModifyVote(Bundle selected) {
        interactor.changeVote(this, selected);
    }

    @Override
    public void onReturnValue(int value, int request, Bundle object) {
        switch (request){
            case 1:
                view.searchAndUpdateItem(object);
                break;
            case 3:
                view.searchAndDeleteItem(object);
                break;
        }
        view.notifyCallReturn(value);
    }

    @Override
    public Context getActivityContext() {
        return view.getActivityContext();
    }


}
