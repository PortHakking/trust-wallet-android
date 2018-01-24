package com.wallet.crypto.trustapp.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.wallet.crypto.trustapp.C;
import com.wallet.crypto.trustapp.entity.GasSettings;
import com.wallet.crypto.trustapp.entity.Transaction;
import com.wallet.crypto.trustapp.entity.Wallet;
import com.wallet.crypto.trustapp.interact.FetchGasSettingsInteract;
import com.wallet.crypto.trustapp.router.ConfirmationRouter;
import com.wallet.crypto.trustapp.util.BalanceUtils;

import java.math.BigInteger;

public class SendViewModel extends BaseViewModel {
    private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();
    private final MutableLiveData<Transaction> transaction = new MutableLiveData<>();
    private final MutableLiveData<GasSettings> gasSettings = new MutableLiveData<>();

    private final ConfirmationRouter confirmationRouter;
    private final FetchGasSettingsInteract fetchGasSettingsInteract;

    private boolean confirmationForTokenTransfer = false;

    public SendViewModel(ConfirmationRouter confirmationRouter, FetchGasSettingsInteract fetchGasSettingsInteract) {
        this.confirmationRouter = confirmationRouter;
        this.fetchGasSettingsInteract = fetchGasSettingsInteract;
    }

    public MutableLiveData<GasSettings> gasSettings() {
        return gasSettings;
    }

    public void prepare(boolean confirmationForTokenTransfer) {
        this.confirmationForTokenTransfer = confirmationForTokenTransfer;
        if (gasSettings.getValue() == null) {
            disposable = fetchGasSettingsInteract
                    .fetch(confirmationForTokenTransfer)
                    .subscribe(this::onGasSettings, this::onError);
        }
    }

    private void onGasSettings(GasSettings gasSettings) {
        this.gasSettings.postValue(gasSettings);
    }

    public void openConfirmation(Context context, String to, BigInteger amount, String contractAddress, int decimals, String symbol, boolean sendingTokens) {
        confirmationRouter.open(context, to, amount, contractAddress, decimals, symbol, sendingTokens);
    }
}
