package com.example.comp90018_2020_sem2_project.ui.broadcastMsg;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BroadcastMsgViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public BroadcastMsgViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
}