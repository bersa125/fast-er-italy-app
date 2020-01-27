package com.presentation_layer.fasteritaly.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.fasteritaly.R;
import com.presentation_layer.fasteritaly.activity.array_adapter.ResultsAdapter;
import com.presentation_layer.fasteritaly.activity.recyclerview_adapter.SettingsAddressesRecyclerViewAdapter;
import com.presentation_layer.fasteritaly.activity.recyclerview_adapter.SettingsMainRecyclerViewAdapter;
import com.presentation_layer.fasteritaly.interactor.SettingsFragmentInteractor;
import com.presentation_layer.fasteritaly.presenter.SettingsFragmentPresenter;
import com.presentation_layer.fasteritaly.view.SettingsFragmentView;
import com.presentation_layer.fasteritaly.view.SettingsView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class SettingsFragment extends Fragment implements SettingsFragmentView {

    private int currentPage;
    private SettingsFragmentPresenter presenter;

    private View thisView;
    private ViewGroup container;
    private LayoutInflater inflater;

    //View components
    private RecyclerView recyclerView;

    private AutoCompleteTextView insertAddress_editTextView;
    private Button addAddressButton;
    private View progressBar;

    private TextView seekBar_value;
    private SeekBar results_seekBar;
    private SwitchCompat service_switchCompat;
    private ScrollView scrollView;

    private SettingsMainRecyclerViewAdapter mainAdapter;
    private SettingsAddressesRecyclerViewAdapter addressAdapter;

    private List<String> dataAddress;
    private List<Bundle> dataMenu;
    private Set<String> searchAddressList;
    private Bundle userSettings;

    private AlertDialog alert;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(int page) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putInt("PAGE",page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        currentPage=getArguments().getInt("PAGE");
        presenter=new SettingsFragmentPresenter(this,new SettingsFragmentInteractor());
        this.container=container;
        this.inflater=inflater;
        presenter.changeAspect(currentPage);
        return thisView;
    }

    @Override
    public void populateAddresses(List<String> adds) {
        if(currentPage== SettingsView.ADDRESSES){
            stopLoadingProgress();
            dataAddress.clear();
            dataAddress.addAll(adds);
            addressAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void populateOptions(Bundle options) {// RESULTS_SHOWED (int), TRACKING (boolean)
        if(currentPage== SettingsView.OPTIONS){
            if(userSettings==null)
                userSettings=new Bundle();
            userSettings.putInt("RESULTS_SHOWED",options.getInt("RESULTS_SHOWED"));
            userSettings.putBoolean("TRACKING",options.getBoolean("TRACKING"));
            results_seekBar.setProgress(userSettings.getInt("RESULTS_SHOWED")-5);
            seekBar_value.setText(userSettings.getInt("RESULTS_SHOWED")+"");
            service_switchCompat.setChecked(userSettings.getBoolean("TRACKING"));
        }
    }

    @Override
    public void modifyAddress(String address, int position) {
        if(currentPage== SettingsView.ADDRESSES){
            dataAddress.set(position,address);
            addressAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void deleteAddress(int position) {
        if(currentPage== SettingsView.ADDRESSES){
            dataAddress.remove(position);
            addressAdapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void addAddress(String address) {
        if(currentPage== SettingsView.ADDRESSES){
            searchAddressList=new HashSet<>();
            dataAddress.add(address);
            addressAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showAddressModDialog(final String address, final int position) {
        if(currentPage== SettingsView.ADDRESSES && address!=null){
            final InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_modify_address, null);
            final AutoCompleteTextView text=dialogView.findViewById(R.id.autoCompleteEditText);
            text.setText(address);
            ((Button) dialogView.findViewById(R.id.update_button)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imm.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                    alert.dismiss();
                    presenter.onAddressModify(text.getText().toString(),position);
                }
            });
            ((Button) dialogView.findViewById(R.id.delete_button)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imm.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                    alert.dismiss();
                    presenter.onAddressDelete(position);
                }
            });
            ((Button) dialogView.findViewById(R.id.cancel_button)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imm.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
                    alert.dismiss();
                }
            });
            builder.setView(dialogView);
            builder.setCancelable(true);
            alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void updateSearchResults(List<String> results) {//Only for the search
        if(results!=null) {
            searchAddressList.clear();
            searchAddressList.addAll(results);
            LinkedList<String> buff=new LinkedList<>();
            for(String s: searchAddressList){
                buff.addFirst(s);
            }
            insertAddress_editTextView.setAdapter(new ResultsAdapter(getContext(), insertAddress_editTextView.getId(), buff));
        }
    }

    @Override
    public void changeCurrentContent(int choice) {
        switch (choice){
            case SettingsView.ADDRESSES:
                thisView=inflater.inflate(R.layout.fragment_settings_addresses, container, false);
                this.recyclerView=thisView.findViewById(R.id.recycler_view);
                this.insertAddress_editTextView=thisView.findViewById(R.id.autoCompleteEditText);
                this.addAddressButton=thisView.findViewById(R.id.submit_address_button);
                this.scrollView=thisView.findViewById(R.id.scrollView);
                progressBar=thisView.findViewById(R.id.progressBar);
                //setup recyclerView
                dataAddress=new ArrayList<>();
                RecyclerView.LayoutManager manager=new LinearLayoutManager(this.getActivity());
                recyclerView.setLayoutManager(manager);
                addressAdapter = new SettingsAddressesRecyclerViewAdapter(this.getActivity(), dataAddress,this);
                recyclerView.setAdapter(addressAdapter);
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), ((LinearLayoutManager) manager).getOrientation());
                recyclerView.addItemDecoration(dividerItemDecoration);
                //setup editTextView
                searchAddressList=new HashSet<>();
                insertAddress_editTextView.setAdapter(new ResultsAdapter(getContext(),insertAddress_editTextView.getId(),new LinkedList<String>()));
                insertAddress_editTextView.setThreshold(5);//Numero di lettere
                insertAddress_editTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        presenter.onAddressImmission(s.toString());
                    }
                    @Override
                    public void afterTextChanged(Editable s) {}
                });
                final InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                addAddressButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imm.hideSoftInputFromWindow(thisView.getWindowToken(), 0);
                        presenter.onAddressInsertion(insertAddress_editTextView.getText().toString());
                    }
                });
                presenter.onAddressUpdate();
                break;
            case SettingsView.OPTIONS:
                thisView=inflater.inflate(R.layout.fragment_settings_options, container, false);
                results_seekBar=thisView.findViewById(R.id.seek_bar_results);
                service_switchCompat=thisView.findViewById(R.id.switch_background_service);
                seekBar_value=thisView.findViewById(R.id.seek_bar_value);
                if(userSettings==null)
                    userSettings=new Bundle();
                results_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        seekBar_value.setText((results_seekBar.getProgress()+5)+"");
                        userSettings.putInt("RESULTS_SHOWED",progress+5);
                        presenter.onSettingsModify(userSettings);
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        seekBar_value.setText((results_seekBar.getProgress()+5)+"");
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        seekBar_value.setText((results_seekBar.getProgress()+5)+"");
                    }
                });
                service_switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        userSettings.putBoolean("TRACKING",isChecked);
                        presenter.onSettingsModify(userSettings);
                    }
                });
                presenter.onSettingsUpdate();
                break;
            case SettingsView.MAIN:
                thisView=inflater.inflate(R.layout.fragment_settings_main, container, false);
                this.recyclerView=thisView.findViewById(R.id.recycler_view);
                manager=new LinearLayoutManager(this.getActivity());
                recyclerView.setLayoutManager(manager);
                dataMenu=new LinkedList<>();
                Bundle option=new Bundle();
                option.putParcelable("IMAGE",getBitmapFromVectorDrawable(getContext(),R.drawable.menu_address));
                option.putString("TEXT",getString(R.string.menu_addresses));
                option.putInt("VALUE",SettingsView.ADDRESSES);
                dataMenu.add(option);
                option=new Bundle();
                option.putParcelable("IMAGE", getBitmapFromVectorDrawable(getContext(),R.drawable.icon_options));
                option.putString("TEXT",getString(R.string.user_options));
                option.putInt("VALUE",SettingsView.OPTIONS);
                dataMenu.add(option);
                this.mainAdapter=new SettingsMainRecyclerViewAdapter(getContext(),dataMenu,this);
                recyclerView.setAdapter(mainAdapter);
                dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), ((LinearLayoutManager) manager).getOrientation());
                recyclerView.addItemDecoration(dividerItemDecoration);
                break;
        }
    }

    @Override
    public void changeContent(int choice) {
        presenter.onChangeAspect(choice);
    }

    @Override
    public String getAddress(int position) {
        if(currentPage== SettingsView.ADDRESSES){
            return dataAddress.get(position);
        }else {
            return null;
        }
    }

    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    @Override
    public void startLoadingProgress() {
        if(progressBar!=null)
            progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void stopLoadingProgress() {
        if(progressBar!=null)
            progressBar.setVisibility(View.GONE);
    }
}

