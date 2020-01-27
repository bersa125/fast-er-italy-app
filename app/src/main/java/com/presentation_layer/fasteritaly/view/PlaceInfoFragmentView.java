package com.presentation_layer.fasteritaly.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public interface PlaceInfoFragmentView {
    //All integers of types
    int TYPE_NAME_ADDRESS=0;
    int TYPE_STATE_ER=1;
    int TYPE_ER_GENERAL_VOTES=2;
    int TYPE_DRUG_OPENING_TIME=3;

    void setCorrectLayout(int type, LayoutInflater inflater, ViewGroup container);
    void updateFragment(int type, Bundle newData);

    Object getActivity();
    int getType();
}
