package com.presentation_layer.fasteritaly.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import java.util.List;

public interface SettingsView {
    int MAIN=0;
    int ADDRESSES=1;
    int OPTIONS=2;

    int ALL_DONE=0;
    int REJECTED =1;
    int ERROR=2;

    void changeCurrentContent(int choice);

    void populateAddresses();
    void populateOptions();

    void updateOptions(Bundle options);
    void modifyAddress(String newAddress, int position);
    void deleteAddress(int pos);
    void addAddress(String address);

    List<Object> getActiveFragments();
    int getCurrentLoadedFragment();

    Activity getActivity();
    void showResultMessage(int msg);
}
