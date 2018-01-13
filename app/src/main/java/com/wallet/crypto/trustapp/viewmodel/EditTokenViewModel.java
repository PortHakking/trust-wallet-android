package com.wallet.crypto.trustapp.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.wallet.crypto.trustapp.interact.FindDefaultWalletInteract;
import com.wallet.crypto.trustapp.router.MyTokensRouter;

/**
 * Created by James on 13/01/2018.
 */


public class EditTokenViewModel extends BaseViewModel
{
    public EditTokenViewModel() {

    }

    public void save(String address, String symbol, int decimals) {
    }

    private void onSaved() {
        progress.postValue(false);
    }

    /*public void showTokens(Context context) {
        findDefaultWalletInteract
                .find()
                .subscribe(w -> myTokensRouter.open(context, w), this::onError);

    }*/
}
