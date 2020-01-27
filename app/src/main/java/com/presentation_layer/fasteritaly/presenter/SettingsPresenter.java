package com.presentation_layer.fasteritaly.presenter;

import android.os.Bundle;

import com.presentation_layer.fasteritaly.interactor.SettingsInteractor;
import com.presentation_layer.fasteritaly.view.SettingsFragmentView;
import com.presentation_layer.fasteritaly.view.SettingsView;

import java.util.List;

public class SettingsPresenter implements SettingsInteractor.onSettingsActivityInteractionListener {

    private SettingsInteractor interactor;
    private SettingsView view;

    public SettingsPresenter(SettingsView v,SettingsInteractor i){
        view=v;
        interactor=i;
    }

    public void onResume(int choice) {
        view.changeCurrentContent(choice);
    }

    @Override
    public void changeContent(int choice) {
        view.changeCurrentContent(choice);
        if(view.getActiveFragments()!=null){
            for(Object fragment: view.getActiveFragments()){
                if(fragment instanceof SettingsFragmentView){
                    SettingsFragmentView v=(SettingsFragmentView) fragment;
                    v.changeCurrentContent(choice);
                    break;
                }
            }
        }
    }

    @Override
    public void onSettingsUpdate() {
        interactor.getSettings(this);
    }

    @Override
    public void onSettingsModification(Bundle newSettings) {
        interactor.updateSettings(this,newSettings);
    }

    @Override
    public void onSettingsViewUpdate(Bundle settings) {
        if(view.getActiveFragments()!=null){
            for(Object fragment: view.getActiveFragments()){
                if(fragment instanceof SettingsFragmentView){
                    SettingsFragmentView v=(SettingsFragmentView) fragment;
                    v.populateOptions(settings);
                    break;
                }
            }
        }
    }

    @Override
    public void onAddressModify(String newAddress, int oldposition) {
        if(view.getActiveFragments()!=null){
            for(Object fragment: view.getActiveFragments()){
                if(fragment instanceof SettingsFragmentView && view.getCurrentLoadedFragment()==view.ADDRESSES){
                    SettingsFragmentView v=(SettingsFragmentView) fragment;
                    interactor.modifyAddress(this,newAddress,v.getAddress(oldposition),oldposition);
                    break;
                }
            }
        }

    }

    @Override
    public void onAddressModified(String newAddress, int oldposition) {
        if(view.getActiveFragments()!=null){
            for(Object fragment: view.getActiveFragments()){
                if(fragment instanceof SettingsFragmentView && view.getCurrentLoadedFragment()==view.ADDRESSES){
                    SettingsFragmentView v=(SettingsFragmentView) fragment;
                    v.modifyAddress(newAddress,oldposition);
                    break;
                }
            }
        }
    }

    @Override
    public void onAddressInsertion(String address) {
        if(view.getActiveFragments()!=null){
            for(Object fragment: view.getActiveFragments()){
                if(fragment instanceof SettingsFragmentView && view.getCurrentLoadedFragment()==view.ADDRESSES){
                    SettingsFragmentView v=(SettingsFragmentView) fragment;
                    interactor.addAddress(this,address);
                    break;
                }
            }
        }
    }

    @Override
    public void onAddressInserted(String address) {
        if(view.getActiveFragments()!=null){
            for(Object fragment: view.getActiveFragments()){
                if(fragment instanceof SettingsFragmentView && view.getCurrentLoadedFragment()==view.ADDRESSES){
                    SettingsFragmentView v=(SettingsFragmentView) fragment;
                    v.addAddress(address);
                    break;
                }
            }
        }
    }


    @Override
    public void onAddressDelete(int position) {
        if(view.getActiveFragments()!=null){
            for(Object fragment: view.getActiveFragments()){
                if(fragment instanceof SettingsFragmentView && view.getCurrentLoadedFragment()==view.ADDRESSES){
                    SettingsFragmentView v=(SettingsFragmentView) fragment;
                    interactor.deleteAddress(this,v.getAddress(position),position);
                    break;
                }
            }
        }

    }

    @Override
    public void onAddressDeleted(int position) {
        if(view.getActiveFragments()!=null){
            for(Object fragment: view.getActiveFragments()){
                if(fragment instanceof SettingsFragmentView && view.getCurrentLoadedFragment()==view.ADDRESSES){
                    SettingsFragmentView v=(SettingsFragmentView) fragment;
                    v.deleteAddress(position);
                    break;
                }
            }
        }
    }

    @Override
    public void onAddressUpdate() {
        interactor.getAddresses(this);
    }

    @Override
    public void onAddressViewUpdate(List<String> addresses) {
        if(view.getActiveFragments()!=null){
            for(Object fragment: view.getActiveFragments()){
                if(fragment instanceof SettingsFragmentView && view.getCurrentLoadedFragment()==view.ADDRESSES){
                    SettingsFragmentView v=(SettingsFragmentView) fragment;
                    v.populateAddresses(addresses);
                    break;
                }
            }
        }
    }

    @Override
    public void onResultMessage(int result) {
        view.showResultMessage(result);
    }

    @Override
    public Object getActivityContext() {
        return view.getActivity();
    }


}
