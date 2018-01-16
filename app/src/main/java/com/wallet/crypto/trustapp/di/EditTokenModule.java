package com.wallet.crypto.trustapp.di;

import com.wallet.crypto.trustapp.interact.AddTokenInteract;
import com.wallet.crypto.trustapp.interact.FindDefaultWalletInteract;
import com.wallet.crypto.trustapp.repository.TokenRepositoryType;
import com.wallet.crypto.trustapp.repository.WalletRepositoryType;
import com.wallet.crypto.trustapp.router.MyTokensRouter;
import com.wallet.crypto.trustapp.viewmodel.AddTokenViewModelFactory;
import com.wallet.crypto.trustapp.viewmodel.EditTokenViewModelFactory;

import dagger.Module;
import dagger.Provides;

/**
 * Created by James on 12/01/2018.
 */


@Module
public class EditTokenModule {
    @Provides
    EditTokenViewModelFactory editTokenViewModelFactory(
        AddTokenInteract addTokenInteract,
        FindDefaultWalletInteract findDefaultWalletInteract,
        MyTokensRouter myTokensRouter)
    {
        return new EditTokenViewModelFactory(
                addTokenInteract, findDefaultWalletInteract, myTokensRouter);
    }

    @Provides
    AddTokenInteract provideAddTokenInteract(
            TokenRepositoryType tokenRepository,
            WalletRepositoryType walletRepository) {
        return new AddTokenInteract(walletRepository, tokenRepository);
    }

    @Provides
    FindDefaultWalletInteract provideFindDefaultWalletInteract(WalletRepositoryType walletRepository) {
        return new FindDefaultWalletInteract(walletRepository);
    }

    @Provides
    MyTokensRouter provideMyTokensRouter() {
        return new MyTokensRouter();
    }
}

