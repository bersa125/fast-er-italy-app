package com.presentation_layer.fasteritaly.interactor;

public class MainPageGridFragmentInteractor {

    public interface OnFragmentInteractionListener{
        void onPopulate(int grid_position);
        void onTap(int grid_position);
    }

}
