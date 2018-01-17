package com.wallet.crypto.trustapp.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.Nullable;

import com.wallet.crypto.trustapp.entity.NetworkInfo;
import com.wallet.crypto.trustapp.entity.Transaction;
import com.wallet.crypto.trustapp.entity.Wallet;
import com.wallet.crypto.trustapp.interact.FetchGasSettingsInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultNetworkInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultWalletInteract;
import com.wallet.crypto.trustapp.interact.GetCurrentPrice;
import com.wallet.crypto.trustapp.interact.GetDefaultWalletBalance;
import com.wallet.crypto.trustapp.router.ConfirmationRouter;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class SendViewModel extends BaseViewModel {
    private static final long GET_BALANCE_INTERVAL = 10;
    private static final long GET_PRICE_INTERVAL = 10;

    private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
    private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();
    private final MutableLiveData<Map<String, String>> defaultWalletBalance = new MutableLiveData<>();
    private final MutableLiveData<String> defaultPrice = new MutableLiveData<>();
    private final MutableLiveData<Transaction> transaction = new MutableLiveData<>();

    private final ConfirmationRouter confirmationRouter;
    private final FetchGasSettingsInteract fetchGasSettingsInteract;
    private final FindDefaultNetworkInteract findDefaultNetworkInteract;
    private final FindDefaultWalletInteract findDefaultWalletInteract;
    private final GetDefaultWalletBalance getDefaultWalletBalance;
    private final GetCurrentPrice getCurrentPrice;

    @Nullable
    private Disposable getBalanceDisposable;

    public SendViewModel(ConfirmationRouter confirmationRouter, FetchGasSettingsInteract fetchGasSettingsInteract, FindDefaultNetworkInteract findDefaultNetworkInteract, FindDefaultWalletInteract findDefaultWalletInteract, GetDefaultWalletBalance getDefaultWalletBalance, GetCurrentPrice getCurrentPrice) {
        this.confirmationRouter = confirmationRouter;
        this.fetchGasSettingsInteract = fetchGasSettingsInteract;
        this.findDefaultNetworkInteract = findDefaultNetworkInteract;
        this.findDefaultWalletInteract = findDefaultWalletInteract;
        this.getDefaultWalletBalance = getDefaultWalletBalance;
        this.getCurrentPrice = getCurrentPrice;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (getBalanceDisposable != null) {
            getBalanceDisposable.dispose();
        }
    }

    public LiveData<NetworkInfo> defaultNetwork() {
        return defaultNetwork;
    }

    public LiveData<Wallet> defaultWallet() {
        return defaultWallet;
    }

    public LiveData<Map<String, String>> defaultWalletBalance() {
        return defaultWalletBalance;
    }

    public LiveData<String> defaultPrice() {
        return defaultPrice;
    }

    public void openConfirmation(Context context, String to, BigInteger amount, String contractAddress, int decimals, String symbol, boolean sendingTokens) {
        confirmationRouter.open(context, to, amount, contractAddress, decimals, symbol, sendingTokens);
    }

    public void getBalance() {
        getBalanceDisposable = Observable.interval(0, GET_BALANCE_INTERVAL, TimeUnit.SECONDS)
                .doOnNext(l -> getDefaultWalletBalance
                        .get(defaultWallet.getValue())
                        .subscribe(defaultWalletBalance::postValue, t -> {
                        }))
                .subscribe(l -> {
                }, t -> {
                });
    }

    public void getPrice() {
        getBalanceDisposable = Observable.interval(0, GET_PRICE_INTERVAL, TimeUnit.SECONDS)
                .doOnNext(l -> getCurrentPrice
                        .getPrice()
                        .subscribe(defaultPrice::postValue, t -> {
                        }))
                .subscribe(l -> {
                }, t -> {
                });
    }

    public void prepare() {
        progress.postValue(true);
        disposable = findDefaultNetworkInteract
                .find()
                .subscribe(this::onDefaultNetwork, this::onError);
    }

    private void onDefaultWallet(Wallet wallet) {
        defaultWallet.setValue(wallet);
        getBalance();
        getPrice();
    }

    private void onDefaultNetwork(NetworkInfo networkInfo) {
        defaultNetwork.postValue(networkInfo);
        disposable = findDefaultWalletInteract
                .find()
                .subscribe(this::onDefaultWallet, this::onError);
    }
}
