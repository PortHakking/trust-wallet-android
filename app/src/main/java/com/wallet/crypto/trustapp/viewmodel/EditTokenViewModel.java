package com.wallet.crypto.trustapp.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import com.wallet.crypto.trustapp.entity.Token;
import com.wallet.crypto.trustapp.entity.TokenInfo;
import com.wallet.crypto.trustapp.entity.Wallet;
import com.wallet.crypto.trustapp.interact.AddTokenInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultWalletInteract;
import com.wallet.crypto.trustapp.router.MyTokensRouter;

import java.math.BigDecimal;

/**
 * Created by James on 13/01/2018.
 */
public class EditTokenViewModel extends BaseViewModel
{
    private final AddTokenInteract addTokenInteract;
    private final FindDefaultWalletInteract findDefaultWalletInteract;
    private final MyTokensRouter myTokensRouter;

    private final MutableLiveData<Boolean> result = new MutableLiveData<>();

    EditTokenViewModel(
            AddTokenInteract addTokenInteract,
            FindDefaultWalletInteract findDefaultWalletInteract,
            MyTokensRouter myTokensRouter) {
        this.addTokenInteract = addTokenInteract;
        this.findDefaultWalletInteract = findDefaultWalletInteract;
        this.myTokensRouter = myTokensRouter;
    }

//    private Token[] getBalances(Wallet wallet, TokenInfo[] items) {
//        int len = items.length;
//        Token[] result = new Token[len];
//        for (int i = 0; i < len; i++) {
//            BigDecimal balance = null;
//            String name = null;
//            try {
//                balance = getBalance(wallet, items[i]);
//                name = getSymbol(wallet, items[i]);
//            } catch (Exception e1) {
//                Log.d("TOKEN", "Err", e1);
//                                    /* Quietly */
//            }
//            System.out.println("NAME: " + name);
//            result[i] = new Token(items[i], balance);
//        }
//        return result;
//    }

    //WORKING HERE: SAVE TOKEN
    public void save(String address, String symbol, int decimals) {
        //we validated the address, now try to find the required values
        addTokenInteract
                .update(address, symbol, decimals)
                .subscribe(this::onSaved, this::onError);
    }

    public void showTokens(Context context) {
        findDefaultWalletInteract
                .find()
                .subscribe(w -> myTokensRouter.open(context, w), this::onError);
    }

    public LiveData<Boolean> result() {
        return result;
    }

    private void onSaved() {
        progress.postValue(false);
        result.postValue(true);
    }

    /*public void showTokens(Context context) {
        findDefaultWalletInteract
                .find()
                .subscribe(w -> myTokensRouter.open(context, w), this::onError);

    }*/
}
