package com.presentation_layer.fasteritaly.view;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

public interface PlaceInfoView {
    int ALL_DONE=0;
    int REJECTED =1;
    int ERROR=2;

    //The boolean tells to the Activity if the Fragment has to be made Invisible
    void setTitleAndTheme(Bundle object);
    void setPlaceImage(Bundle object,boolean isInvisible);
    void setAddressContent(Bundle object,boolean isInvisible);
    void setVotesContent(Bundle object,boolean isInvisible);
    void setERQueueState(Bundle object,boolean isInvisible);
    void setDrugOpeningState(Bundle object,boolean isInvisible);

    void requestUpdate(Bundle object);
    void showVoteInsertionDialog();
    void ShowUserVoteSubmissionMessageResult(int result);

    List<Object> getActiveFragments();
    boolean getDetailsType();
    Context getContext();

    void checkInternetFunctionality();
    void stopcheckInternetFunctionality();

}
