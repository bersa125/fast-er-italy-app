package com.presentation_layer.fasteritaly.view;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

public interface HistoryView {
    int ALL_DONE=0;
    int REJECTED =1;
    int ERROR=2;
    int REJECTED_MODIFY = 3;

    void populateHistoryView(List<Bundle> items);
    void searchAndDeleteItem(Bundle object);
    void searchAndUpdateItem(Bundle object);
    void showModificationsDialog(Bundle object);
    void notifyCallReturn(int result);
    void clearResults();

    void startLoadingScreen();
    void stopLoadingScreen();

    void checkInternetFunctionality();
    void stopcheckInternetFunctionality();

    List<Bundle> getItemArrayList();
    Context getActivityContext();
}
