package com.wallet.crypto.trustapp.di;

import com.wallet.crypto.trustapp.interact.FindDefaultWalletInteract;
import com.wallet.crypto.trustapp.repository.TokenRepositoryType;
import com.wallet.crypto.trustapp.repository.WalletRepositoryType;
import com.wallet.crypto.trustapp.router.MyTokensRouter;
import com.wallet.crypto.trustapp.viewmodel.EditTokenViewModelFactory;

import dagger.Module;
import dagger.Provides;

/**
 * Created by James on 12/01/2018.
 */


@Module
public class EditTokenModule
{

    @Provides
    EditTokenViewModelFactory editTokenViewModelFactory(
            ) {
        return new EditTokenViewModelFactory(
                );
    }

    /*@Provides
    EditTokenInteract provideEditTokenInteract(
            TokenRepositoryType tokenRepository,
            WalletRepositoryType walletRepository) {
        return new EditTokenInteract(walletRepository, tokenRepository);
    }

    @Provides
    FindDefaultWalletInteract provideFindDefaultWalletInteract(WalletRepositoryType walletRepository) {
        return new FindDefaultWalletInteract(walletRepository);
    }

    @Provides
    MyTokensRouter provideMyTokensRouter() {
        return new MyTokensRouter();
    }*/
}

