package com.presentation_layer.fasteritaly.presenter;

import android.os.Bundle;

import com.presentation_layer.fasteritaly.interactor.SettingsFragmentInteractor;
import com.presentation_layer.fasteritaly.view.SettingsFragmentView;
import com.presentation_layer.fasteritaly.view.SettingsView;

import java.util.List;


public class SettingsFragmentPresenter implements SettingsFragmentInteractor.onSettingsFragmentInteractionListener {

    private SettingsFragmentView view;
    private SettingsFragmentInteractor interactor;

    public SettingsFragmentPresenter(SettingsFragmentView v, SettingsFragmentInteractor i){
        view=v;
        interactor= i;
    }

    @Override
    public void changeAspect(int choice) {
        view.changeCurrentContent(choice);
    }

    @Override
    public void onChangeAspect(int choice) {
        if(view.getActivity()!=null){
            if(view.getActivity() instanceof SettingsView){
                ((SettingsView)view.getActivity()).changeCurrentContent(choice);
            }
        }
    }

    @Override
    public void onAddressImmission(String searched) {
        interactor.findPlaces(this,view.getActivity(),searched);
    }

    @Override
    public void onAddressResults(List<String> results) {
        view.updateSearchResults(results);
    }

    @Override
    public void onSettingsUpdate() {
        if(view.getActivity()!=null){
            if(view.getActivity() instanceof SettingsView){
                ((SettingsView)view.getActivity()).populateOptions();
            }
        }
    }

    @Override
    public void onSettingsModify(Bundle newSettings) {
        if(view.getActivity()!=null){
            if(view.getActivity() instanceof SettingsView){
                ((SettingsView)view.getActivity()).updateOptions(newSettings);
            }
        }
    }

    @Override
    public void onAddressModify(String newAddress, int oldposition) {
        if(view.getActivity()!=null){
            if(view.getActivity() instanceof SettingsView){
                ((SettingsView)view.getActivity()).modifyAddress(newAddress,oldposition);
            }
        }
    }

    @Override
    public void onAddressInsertion(String address) {
        if(view.getActivity()!=null){
            if(view.getActivity() instanceof SettingsView){
                ((SettingsView)view.getActivity()).addAddress(address);
            }
        }
    }

    @Override
    public void onAddressDelete(int position) {
        if(view.getActivity()!=null){
            if(view.getActivity() instanceof SettingsView){
                ((SettingsView)view.getActivity()).deleteAddress(position);
            }
        }
    }

    @Override
    public void onAddressUpdate() {
        if(view.getActivity()!=null){
            if(view.getActivity() instanceof SettingsView){
                view.startLoadingProgress();
                ((SettingsView)view.getActivity()).populateAddresses();
            }
        }
    }

}
