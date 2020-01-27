package com.presentation_layer.fasteritaly.activity;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fasteritaly.R;
import com.presentation_layer.fasteritaly.interactor.MainPageGridFragmentInteractor;
import com.presentation_layer.fasteritaly.presenter.MainPageGridFragmentPresenter;
import com.presentation_layer.fasteritaly.view.MainPageGridFragmentView;


public class MainPageGridFragment extends Fragment implements MainPageGridFragmentView {

    private MainPageGridFragmentPresenter presenter;
    private View thisView;

    public MainPageGridFragment() {
        // Required empty public constructor
    }

    public static MainPageGridFragment newInstance() {
        MainPageGridFragment fragment = new MainPageGridFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(presenter==null){
            presenter=new MainPageGridFragmentPresenter(this, new MainPageGridFragmentInteractor());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        thisView=inflater.inflate(R.layout.fragment_main_page_grid, container, false);
        if(presenter==null){
            presenter=new MainPageGridFragmentPresenter(this, new MainPageGridFragmentInteractor());
        }
        final int tag=Integer.parseInt(this.getTag());
        thisView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onTap(tag);
            }
        });
        presenter.onCreateView(tag);
        return thisView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(presenter==null){
            presenter=new MainPageGridFragmentPresenter(this, new MainPageGridFragmentInteractor());
        }
        presenter.onAttach(Integer.parseInt(this.getTag()));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter.onDetach();
    }

    @Override
    public void setAppearance(int grid) {
        if(thisView!=null) {
            switch (grid) {
                case 0:
                    ((TextView) thisView.findViewById(R.id.grid_fragment_title)).setText(R.string.e_r_next_me);
                    thisView.findViewById(R.id.grid_fragment_frame_layout).setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.crimson));
                    ((ImageView) thisView.findViewById(R.id.grid_fragment_image)).setImageResource(R.drawable.icon_hospital);
                    break;
                case 1:
                    ((TextView) thisView.findViewById(R.id.grid_fragment_title)).setText(R.string.e_r_next_addr);
                    thisView.findViewById(R.id.grid_fragment_frame_layout).setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.blueviolet));
                    ((ImageView) thisView.findViewById(R.id.grid_fragment_image)).setImageResource(R.drawable.icon_hospital_address);
                    break;
                case 2:
                    ((TextView) thisView.findViewById(R.id.grid_fragment_title)).setText(R.string.drug_next_me);
                    thisView.findViewById(R.id.grid_fragment_frame_layout).setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.darkorange));
                    ((ImageView) thisView.findViewById(R.id.grid_fragment_image)).setImageResource(R.drawable.icon_pharmacy);
                    break;
                case 3:
                    ((TextView) thisView.findViewById(R.id.grid_fragment_title)).setText(R.string.drug_next_addr);
                    thisView.findViewById(R.id.grid_fragment_frame_layout).setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.lightseagreen));
                    ((ImageView) thisView.findViewById(R.id.grid_fragment_image)).setImageResource(R.drawable.icon_pharmcy_address);
                    break;
                case 4:
                    ((TextView) thisView.findViewById(R.id.grid_fragment_title)).setText(R.string.history);
                    thisView.findViewById(R.id.grid_fragment_frame_layout).setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.olivedrab));
                    ((ImageView) thisView.findViewById(R.id.grid_fragment_image)).setImageResource(R.drawable.icon_history);
                    break;
                case 5:
                    ((TextView) thisView.findViewById(R.id.grid_fragment_title)).setText(R.string.quit);
                    thisView.findViewById(R.id.grid_fragment_frame_layout).setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.red));
                    ((ImageView) thisView.findViewById(R.id.grid_fragment_image)).setImageResource(R.drawable.icon_close);
                    break;
                case 7:
                    ((TextView) thisView.findViewById(R.id.grid_fragment_title)).setText(getString(R.string.menu_settings)+"\n");
                    thisView.findViewById(R.id.grid_fragment_frame_layout).setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.jet));
                    ((ImageView) thisView.findViewById(R.id.grid_fragment_image)).setImageResource(R.drawable.icon_options);
                    break;
                case 8:
                    ((TextView) thisView.findViewById(R.id.grid_fragment_title)).setText(getString(R.string.menu_disconnect)+"\n");
                    thisView.findViewById(R.id.grid_fragment_frame_layout).setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.monsoon));
                    ((ImageView) thisView.findViewById(R.id.grid_fragment_image)).setImageResource(R.drawable.icon_disconnect);
                    break;
                case 9:
                    ((TextView) thisView.findViewById(R.id.grid_fragment_title)).setText(R.string.menu_quit);
                    thisView.findViewById(R.id.grid_fragment_frame_layout).setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.base));
                    ((ImageView) thisView.findViewById(R.id.grid_fragment_image)).setImageResource(R.drawable.icon_exit);
                    break;
            }
        }
    }

    //Method from the View
    @Override
    public Object getAttachedView() {
        return this.getActivity();
    }


}
