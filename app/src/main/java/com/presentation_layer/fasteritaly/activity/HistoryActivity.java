package com.presentation_layer.fasteritaly.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fasteritaly.R;
import com.google.android.material.snackbar.Snackbar;
import com.presentation_layer.fasteritaly.activity.loading_dialog.Loading_Dialog;
import com.presentation_layer.fasteritaly.activity.recyclerview_adapter.HistoryRecyclerViewAdapter;
import com.presentation_layer.fasteritaly.interactor.HistoryInteractor;
import com.presentation_layer.fasteritaly.presenter.HistoryPresenter;
import com.presentation_layer.fasteritaly.view.HistoryView;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements HistoryView {

    private HistoryPresenter presenter;

    List<Bundle> data;

    private View progressBar;
    private RecyclerView recyclerView;
    private TextView noResults;

    private HistoryRecyclerViewAdapter Radapter;

    private Snackbar noInternet;
    private BroadcastReceiver internetSignalReceiver;

    private AlertDialog alert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        presenter=new HistoryPresenter(this,new HistoryInteractor());


        //Title construction with colored text
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String pin = "\u25CF ";
        SpannableString preamble = new SpannableString(pin);
        preamble.setSpan(new RelativeSizeSpan(1.3f), 0,1, 0);
        preamble.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.olivedrab,null)), 0, pin.length(), 0);
        builder.append(preamble);
        SpannableString title = new SpannableString(getString(R.string.title_activity_history));
        title.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white,null)), 0, getString(R.string.title_activity_history).length(), 0);
        builder.append(title);
        setTitle(builder);

        //Hides statusbar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        data=new ArrayList<>();

        progressBar=findViewById(R.id.progressBar);
        recyclerView=findViewById(R.id.recycler_view);
        noResults=findViewById(R.id.no_results_text);

        //recyclerView Init
        RecyclerView.LayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        Radapter = new HistoryRecyclerViewAdapter(this, data,this);
        recyclerView.setAdapter(Radapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), ((LinearLayoutManager) manager).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        noInternet= Snackbar.make(recyclerView, R.string.no_internet, Snackbar.LENGTH_INDEFINITE);

    }

    @Override
    protected void onResume(){
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onPause(){
        presenter.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void populateHistoryView(List<Bundle> items) {
        for(Bundle i: items){
            ((ArrayList<Bundle>)data).add(0,i);
            Radapter.notifyItemChanged(0,i);
        }


        setNoResults(data.isEmpty());
    }

    @Override
    public void searchAndDeleteItem(Bundle object) {
        int position=data.indexOf(object);
        data.remove(position);
        Radapter.notifyItemRemoved(position);
    }

    @Override
    public void searchAndUpdateItem(Bundle object) {
        for(int i=0;i<data.size();i++){
            Bundle b=data.get(i);
            if(b.get("PLACE").equals(object.get("PLACE")) && b.get("DATE").equals(object.get("DATE"))){
                b.putInt("AVG_VOTE_USR",object.getInt("AVG_VOTE_USR"));
                b.putInt("AVG_VOTE_USR_WAIT",object.getInt("AVG_VOTE_USR_WAIT"));
                b.putInt("AVG_VOTE_USR_SERVICE",object.getInt("AVG_VOTE_USR_SERVICE"));
                b.putInt("AVG_VOTE_USR_STRUCT",object.getInt("AVG_VOTE_USR_STRUCT"));
                Radapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void showModificationsDialog(final Bundle object) {
        if(noInternet!=null && object!=null) {
            if(!noInternet.isShown()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_modify_vote_choice, null);
                TextView h_name=dialogView.findViewById(R.id.hospital_name);
                h_name.setText(h_name.getText()+"\n"+object.getString("PLACE"));
                TextView h_address=dialogView.findViewById(R.id.hospital_address);
                h_address.setText(h_address.getText()+"\n"+object.getString("ADDRESS"));
                final RatingBar rate_wait = (RatingBar) dialogView.findViewById(R.id.ratingBar_wait);
                rate_wait.setRating(object.getInt("AVG_VOTE_USR_WAIT",0));
                final RatingBar rate_struct = (RatingBar) dialogView.findViewById(R.id.ratingBar_structure);
                rate_struct.setRating(object.getInt("AVG_VOTE_USR_STRUCT",0));
                final RatingBar rate_service = (RatingBar) dialogView.findViewById(R.id.ratingBar_service);
                rate_service.setRating(object.getInt("AVG_VOTE_USR_SERVICE",0));
                final Activity activity=this;
                ((Button) dialogView.findViewById(R.id.update_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                        Bundle newObj = new Bundle(object);
                        newObj.putInt("AVG_VOTE_USR_WAIT", (int) rate_wait.getRating());
                        newObj.putInt("AVG_VOTE_USR_SERVICE", (int) rate_service.getRating());
                        newObj.putInt("AVG_VOTE_USR_STRUCT", (int) rate_struct.getRating());
                        newObj.putInt("AVG_VOTE_USR", (int) ((rate_wait.getRating() + rate_struct.getRating() + rate_service.getRating()) / 3));
                        Loading_Dialog.getInstance().showDialog(activity,getString(R.string.while_modify));
                        presenter.onModifyVote(newObj);
                    }
                });
                ((Button) dialogView.findViewById(R.id.delete_vote_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                        Loading_Dialog.getInstance().showDialog(activity,getString(R.string.while_delete));
                        presenter.onDeleteVote(object);
                    }
                });
                builder.setView(dialogView);
                builder.setCancelable(true);
                alert = builder.create();
                alert.show();
            }
        }
    }

    @Override
    public void notifyCallReturn(int result) {
        Loading_Dialog.getInstance().dismissDialog();
        switch(result){
            case HistoryView.ERROR:
                Toast.makeText(this,getString(R.string.error_submission),Toast.LENGTH_LONG).show();
                break;
            case HistoryView.REJECTED:
                Toast.makeText(this,getString(R.string.delete_rejected),Toast.LENGTH_LONG).show();
                break;
            case HistoryView.ALL_DONE:
                Toast.makeText(this,getString(R.string.done),Toast.LENGTH_LONG).show();
                break;
            case HistoryView.REJECTED_MODIFY:
                Toast.makeText(this,getString(R.string.modify_rejected),Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void clearResults() {
        data.clear();
        Radapter.notifyDataSetChanged();
    }

    @Override
    public void startLoadingScreen() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void stopLoadingScreen() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public List<Bundle> getItemArrayList() {
        return data;
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    public void checkInternetFunctionality() {
        //Setup the internet checker
        if(internetSignalReceiver==null) {
            internetSignalReceiver= new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ConnectivityManager connectivityManager
                            = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {//Connection not Active
                        noInternet.show();
                    }else{
                        if(noInternet.isShown()){
                            noInternet.dismiss();
                            if(data.isEmpty()){
                                presenter.onHistoryPopulate();
                            }
                        }

                    }
                }
            };
            try {
                registerReceiver(internetSignalReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            }catch(Exception e){}
        }

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(!(activeNetworkInfo != null && activeNetworkInfo.isConnected())){//Connection not Active
            noInternet.show();
        }
    }

    @Override
    public void stopcheckInternetFunctionality() {
        try {
            unregisterReceiver(internetSignalReceiver);
        } catch (IllegalArgumentException e) {
        }
    }

    private void setNoResults(boolean isTrue) {
        if(isTrue)
            noResults.setVisibility(View.VISIBLE);
        else
            noResults.setVisibility(View.GONE);
    }



}
