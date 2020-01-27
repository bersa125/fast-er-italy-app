package com.presentation_layer.fasteritaly.interactor;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.business_logic.fasteritaly.asynctask.HistoryManipulator;
import com.business_logic.fasteritaly.asynctask.PlaceInformationUpdate;
import com.business_logic.fasteritaly.asynctask.PlacePhotoLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.GetTokenResult;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.presentation_layer.fasteritaly.view.PlaceInfoView;

public class PlaceInfoInteractor {
    public interface onPlaceInfoInteractionListener{
        //Choose wisely which fragment use for the specified object
        void onPopulate(Bundle object);

        //Call to submit new Votes
        void onShowSubmissionVote();
        void onSubmitVote(Bundle object,int[] votes);
        void onVoteSubmitted(int result, Bundle object); // result can be ALL_DONE, ERROR, REJECTED

        //Call to ask a page Update
        void onRequestUpdate(Bundle object);
        //Response
        void onUpdateChildNotificationRequest(int child,Bundle object);

        //response to set the Photo
        void onPhotoReady(Bundle object);

        Context getActivityContext();

    }

    public void submitVoteforER(final onPlaceInfoInteractionListener listener, final Bundle object, final int[] votes){
        try {
            CommonAccessData.getInstance().getCurrentFirebaseUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {
                    //Launch the request to the server
                    new HistoryManipulator(listener, getTokenResult.getToken(),object,votes).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        } catch (Exception e) {
            listener.onVoteSubmitted(PlaceInfoView.REJECTED,object);
        }
    }
    public void requestInformationUpdate(onPlaceInfoInteractionListener listener,Bundle object){
        new PlaceInformationUpdate(listener,object).execute();
    }
    public void requestPlacePhoto(onPlaceInfoInteractionListener listener,Bundle object){
        new PlacePhotoLoader(listener,object).execute();
    }
}
