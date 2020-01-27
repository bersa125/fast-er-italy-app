package com.presentation_layer.fasteritaly.view;

import android.os.Bundle;

import java.util.List;

public interface SettingsFragmentView {


    void populateAddresses(List<String> adds);
    void populateOptions(Bundle options);

    void modifyAddress(String address,int position);
    void deleteAddress(int position);
    void addAddress(String address);

    void showAddressModDialog(String address,int position);

    void updateSearchResults(List<String> reuslts);

    //Only at launch
    void changeCurrentContent(int choice);
    //After
    void changeContent(int choice);

    String getAddress(int position);
    Object getActivity();

    void startLoadingProgress();
    void stopLoadingProgress();

}
