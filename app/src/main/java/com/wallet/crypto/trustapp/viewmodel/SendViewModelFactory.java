package com.wallet.crypto.trustapp.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.wallet.crypto.trustapp.interact.FetchGasSettingsInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultNetworkInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultWalletInteract;
import com.wallet.crypto.trustapp.interact.GetCurrentPrice;
import com.wallet.crypto.trustapp.interact.GetDefaultWalletBalance;
import com.wallet.crypto.trustapp.router.ConfirmationRouter;

import io.reactivex.annotations.NonNull;

public class SendViewModelFactory implements ViewModelProvider.Factory {

    private final ConfirmationRouter confirmationRouter;
    private final FetchGasSettingsInteract fetchGasSettingsInteract;
    private final GetDefaultWalletBalance getDefaultWalletBalance;
    private final FindDefaultNetworkInteract findDefaultNetworkInteract;
    private final FindDefaultWalletInteract findDefaultWalletInteract;
    private final GetCurrentPrice getCurrentPrice;

    public SendViewModelFactory(ConfirmationRouter confirmationRouter, FetchGasSettingsInteract fetchGasSettingsInteract, FindDefaultNetworkInteract findDefaultNetworkInteract, FindDefaultWalletInteract findDefaultWalletInteract, GetDefaultWalletBalance getDefaultWalletBalance, GetCurrentPrice getCurrentPrice) {
        this.confirmationRouter = confirmationRouter;
        this.fetchGasSettingsInteract = fetchGasSettingsInteract;
        this.getDefaultWalletBalance = getDefaultWalletBalance;
        this.findDefaultNetworkInteract = findDefaultNetworkInteract;
        this.findDefaultWalletInteract = findDefaultWalletInteract;
        this.getCurrentPrice = getCurrentPrice;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SendViewModel(confirmationRouter, fetchGasSettingsInteract, findDefaultNetworkInteract, findDefaultWalletInteract, getDefaultWalletBalance, getCurrentPrice);
    }
}
