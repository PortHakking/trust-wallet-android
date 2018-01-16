package com.wallet.crypto.trustapp.interact.rx.operator;

import com.wallet.crypto.trustapp.entity.Wallet;
import com.wallet.crypto.trustapp.repository.PasswordStore;
import com.wallet.crypto.trustapp.repository.WalletRepositoryType;

import io.reactivex.Single;
import io.reactivex.SingleTransformer;

import static com.wallet.crypto.trustapp.interact.rx.operator.Operators.completableErrorProxy;

public class SavePasswordOperator implements SingleTransformer<Wallet, Wallet> {

    private final PasswordStore passwordStore;
    private final String password;
    private final WalletRepositoryType walletRepository;

    SavePasswordOperator(
            PasswordStore passwordStore, WalletRepositoryType walletRepository, String password) {
        this.passwordStore = passwordStore;
        this.password = password;
        this.walletRepository = walletRepository;
    }

    @Override
    public Single<Wallet> apply(Single<Wallet> upstream) {
        return upstream.flatMap(wallet ->
                passwordStore
                .setPassword(wallet, password)
                .onErrorResumeNext(err -> walletRepository
                        .deleteWallet(wallet.address, password)
                        .lift(completableErrorProxy(err)))
                .toSingle(() -> wallet));
    }
}
