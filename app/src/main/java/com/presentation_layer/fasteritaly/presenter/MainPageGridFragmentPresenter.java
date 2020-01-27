package com.presentation_layer.fasteritaly.presenter;

import com.presentation_layer.fasteritaly.interactor.MainPageGridFragmentInteractor;
import com.presentation_layer.fasteritaly.view.MainPageGridFragmentView;
import com.presentation_layer.fasteritaly.view.MainPageView;

public class MainPageGridFragmentPresenter implements MainPageGridFragmentInteractor.OnFragmentInteractionListener {

    private MainPageGridFragmentInteractor interactor;
    private MainPageGridFragmentView view;

    public MainPageGridFragmentPresenter(MainPageGridFragmentView v, MainPageGridFragmentInteractor i){
        interactor=i;
        view=v;
    }

    //Mehods related to something that happens in the View
    public void onAttach(int grid_position){
        onPopulate(grid_position);
    }
    public void onDetach(){}
    public void onCreateView(int grid_position){
        onPopulate(grid_position);
    }

    @Override
    public void onPopulate(int grid_position) {
        view.setAppearance(grid_position);
    }
    @Override
    public void onTap(int grid_position) {
        //Recalls from the MainPageView the page change
        if (view instanceof MainPageGridFragmentView) {
            if(view.getAttachedView() instanceof MainPageView) {
                if(grid_position<5 || grid_position==6 || grid_position==7) //Only if other
                    ((MainPageView) view.getAttachedView()).launchActivity(grid_position);
                else {
                    if(grid_position==5)
                        ((MainPageView) view.getAttachedView()).closeApp();
                    else if(grid_position==8){
                        ((MainPageView) view.getAttachedView()).disconnect();
                    }else{
                        ((MainPageView) view.getAttachedView()).exitApp();
                    }
                }
            }
        }
    }
}
