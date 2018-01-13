package com.wallet.crypto.trustapp.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;


/**
 * Created by James on 13/01/2018.
 */


public class EditTokenViewModelFactory implements ViewModelProvider.Factory {

    public EditTokenViewModelFactory() {
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new EditTokenViewModel();
    }
}

