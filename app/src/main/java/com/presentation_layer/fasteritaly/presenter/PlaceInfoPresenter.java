package com.presentation_layer.fasteritaly.presenter;

import android.content.Context;
import android.os.Bundle;

import com.presentation_layer.fasteritaly.interactor.PlaceInfoInteractor;
import com.presentation_layer.fasteritaly.view.PlaceInfoFragmentView;
import com.presentation_layer.fasteritaly.view.PlaceInfoView;

public class PlaceInfoPresenter implements PlaceInfoInteractor.onPlaceInfoInteractionListener {

    private PlaceInfoView view;
    private PlaceInfoInteractor interactor;

    public PlaceInfoPresenter(PlaceInfoView v,PlaceInfoInteractor i){
        view=v;
        interactor=i;
    }

    public void onResume() {
        view.checkInternetFunctionality();
    }

    public void onPause() {
        view.stopcheckInternetFunctionality();
    }

    public void onDestroy() {
        view.stopcheckInternetFunctionality();
    }

    @Override
    public void onPopulate(Bundle object) {
        view.setTitleAndTheme(object);
        interactor.requestPlacePhoto(this,object);
        view.setAddressContent(object, false);
        view.setVotesContent(object,view.getDetailsType());//false => E.R: => !false=true
        view.setERQueueState(object,view.getDetailsType());
        view.setDrugOpeningState(object,!view.getDetailsType());
    }

    @Override
    public void onShowSubmissionVote() {
        view.showVoteInsertionDialog();
    }

    @Override
    public void onSubmitVote(Bundle object, int[] votes) {
        interactor.submitVoteforER(this,object,votes);
    }

    @Override
    public void onVoteSubmitted(int result, Bundle object) {
        if(result==view.ALL_DONE){
            view.setVotesContent(object,view.getDetailsType());
        }
        view.ShowUserVoteSubmissionMessageResult(result);
    }

    @Override
    public void onRequestUpdate(Bundle object) {
        interactor.requestInformationUpdate(this,object);
    }

    @Override
    public void onUpdateChildNotificationRequest(int child, Bundle object) {
        for(Object o: view.getActiveFragments()){
            if(o instanceof PlaceInfoFragmentView){
                ((PlaceInfoFragmentView)o).updateFragment(child,object);
            }
        }
    }

    @Override
    public void onPhotoReady(Bundle object) {
        if(object.get("PLACE_PHOTO")==null){
            view.setPlaceImage(object,true);
        }else{
            view.setPlaceImage(object,false);
        }
    }

    @Override
    public Context getActivityContext() {
        return view.getContext();
    }


}
