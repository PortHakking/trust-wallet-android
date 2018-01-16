package com.wallet.crypto.trustapp.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.wallet.crypto.trustapp.interact.AddTokenInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultWalletInteract;
import com.wallet.crypto.trustapp.router.MyTokensRouter;


/**
 * Created by James on 13/01/2018.
 */


public class EditTokenViewModelFactory implements ViewModelProvider.Factory {
    private final AddTokenInteract addTokenInteract;
    private final FindDefaultWalletInteract findDefaultWalletInteract;
    private final MyTokensRouter myTokensRouter;

    public EditTokenViewModelFactory(
            AddTokenInteract addTokenInteract,
            FindDefaultWalletInteract findDefaultWalletInteract,
            MyTokensRouter myTokensRouter) {
        this.addTokenInteract = addTokenInteract;
        this.findDefaultWalletInteract = findDefaultWalletInteract;
        this.myTokensRouter = myTokensRouter;
    }


    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new EditTokenViewModel(addTokenInteract, findDefaultWalletInteract, myTokensRouter);
    }
}

