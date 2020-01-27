package com.presentation_layer.fasteritaly.interactor;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.business_logic.fasteritaly.asynctask.HistoryManipulator;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.GetTokenResult;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.presentation_layer.fasteritaly.view.HistoryView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HistoryInteractor {

    public interface onHistoryActivityInteractionListener{
        void onHistoryPopulate();
        void onHistoryUpdate(List<Bundle> bundle);
        void onTupleSelection(Bundle selected);

        void onDeleteVote(Bundle selected);
        void onModifyVote(Bundle selected);

        void onReturnValue(int value,int request, Bundle object);

        Context getActivityContext();
    }
    public void changeVote(final onHistoryActivityInteractionListener listener, final Bundle vote){//si riconosce dalla data e dai dati dell'ospedale
        //only asynchronous call with notify
        try {
            CommonAccessData.getInstance().getCurrentFirebaseUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {
                    //Launch the request to the server
                    new HistoryManipulator(listener, getTokenResult.getToken(),vote).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        } catch (Exception e) {
            listener.onReturnValue(HistoryView.REJECTED,-1,vote);
        }
    }
    public void deleteVote(final onHistoryActivityInteractionListener listener, final Bundle vote){
        //only asynchronous call with notify
        try {
            CommonAccessData.getInstance().getCurrentFirebaseUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {
                    //Launch the request to the server
                    new HistoryManipulator(listener, getTokenResult.getToken(),vote,true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        } catch (Exception e) {
            listener.onReturnValue(HistoryView.REJECTED,-1,vote);
        }
    }
    public void updateVotes(final onHistoryActivityInteractionListener listener, final List<Bundle> votes){
        try {
            CommonAccessData.getInstance().getCurrentFirebaseUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {
                    //Launch the request to the server
                    new HistoryManipulator(listener, getTokenResult.getToken(),votes).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        } catch (Exception e) {
            listener.onReturnValue(HistoryView.REJECTED,-1,null);
            listener.onHistoryUpdate(new LinkedList<Bundle>());
        }

    }
}
