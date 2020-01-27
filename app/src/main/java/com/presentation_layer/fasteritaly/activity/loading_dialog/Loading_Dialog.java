package com.presentation_layer.fasteritaly.activity.loading_dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.fasteritaly.R;

public class Loading_Dialog {
    private static Loading_Dialog ourInstance = new Loading_Dialog();

    private Dialog alert;

    public static Loading_Dialog getInstance() {
        if(ourInstance==null){
            ourInstance=new Loading_Dialog();
        }
        return ourInstance;
    }

    private Loading_Dialog() {
    }

    public void showDialog(Activity activity, String message){
        if(alert!=null){
            try {
                alert.dismiss();
            }catch (Exception e){}
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_loading_action, null);
        ((TextView)dialogView.findViewById(R.id.message)).setText(message+"...");
        builder.setView(dialogView);
        builder.setCancelable(false);
        alert = builder.create();
        alert.show();
    }

    public void dismissDialog(){
        if(alert!=null){
            try {
                alert.dismiss();
            }catch (Exception e){}
        }
        alert=null;
    }
}
