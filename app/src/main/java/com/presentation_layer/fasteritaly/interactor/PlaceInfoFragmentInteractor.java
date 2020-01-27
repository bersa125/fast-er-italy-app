package com.presentation_layer.fasteritaly.interactor;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class PlaceInfoFragmentInteractor {
    public interface onPlaceInfoFragmentInteractionListener{
        //sets up the fragment based on the integer that defines the fragment type
        void onPopulate(int i, Bundle object, LayoutInflater inflater, ViewGroup container);
        void onUpdateData( Bundle object);

        //requestes
        void onVoteSubmissionRequest();
        void onActvityUpdateRequest(Bundle object);

        //getContext from view
        Object getViewContext();
    }
}
