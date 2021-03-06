package com.wallet.crypto.trustapp.di;

import android.content.Context;

import com.google.gson.Gson;
import com.wallet.crypto.trustapp.repository.EthereumNetworkRepository;
import com.wallet.crypto.trustapp.repository.EthereumNetworkRepositoryType;
import com.wallet.crypto.trustapp.repository.GasSettingsRepository;
import com.wallet.crypto.trustapp.repository.GasSettingsRepositoryType;
import com.wallet.crypto.trustapp.repository.PreferenceRepositoryType;
import com.wallet.crypto.trustapp.repository.TokensRealmSource;
import com.wallet.crypto.trustapp.repository.SharedPreferenceRepository;
import com.wallet.crypto.trustapp.repository.TokenLocalSource;
import com.wallet.crypto.trustapp.repository.TokenRepository;
import com.wallet.crypto.trustapp.repository.TokenRepositoryType;
import com.wallet.crypto.trustapp.repository.TransactionLocalSource;
import com.wallet.crypto.trustapp.repository.TransactionRepository;
import com.wallet.crypto.trustapp.repository.TransactionRepositoryType;
import com.wallet.crypto.trustapp.repository.TransactionsRealmCache;
import com.wallet.crypto.trustapp.repository.WalletRepository;
import com.wallet.crypto.trustapp.repository.WalletRepositoryType;
import com.wallet.crypto.trustapp.service.AccountKeystoreService;
import com.wallet.crypto.trustapp.service.EthplorerTokenService;
import com.wallet.crypto.trustapp.service.GethKeystoreAccountService;
import com.wallet.crypto.trustapp.service.TickerService;
import com.wallet.crypto.trustapp.service.TokenExplorerClientType;
import com.wallet.crypto.trustapp.service.TransactionsNetworkClient;
import com.wallet.crypto.trustapp.service.TransactionsNetworkClientType;
import com.wallet.crypto.trustapp.service.TrustWalletTickerService;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class RepositoriesModule {
	@Singleton
	@Provides
	PreferenceRepositoryType providePreferenceRepository(Context context) {
		return new SharedPreferenceRepository(context);
	}

	@Singleton
	@Provides
	AccountKeystoreService provideAccountKeyStoreService(Context context) {
        File file = new File(context.getFilesDir(), "keystore/keystore");
		return new GethKeystoreAccountService(file);
	}

	@Singleton
    @Provides
    TickerService provideTickerService(OkHttpClient httpClient, Gson gson) {
	    return new TrustWalletTickerService(httpClient, gson);
    }

	@Singleton
	@Provides
	EthereumNetworkRepositoryType provideEthereumNetworkRepository(
            PreferenceRepositoryType preferenceRepository,
            TickerService tickerService) {
		return new EthereumNetworkRepository(preferenceRepository, tickerService);
	}

	@Singleton
	@Provides
    WalletRepositoryType provideWalletRepository(
            OkHttpClient okHttpClient,
			PreferenceRepositoryType preferenceRepositoryType,
			AccountKeystoreService accountKeystoreService,
			EthereumNetworkRepositoryType networkRepository) {
		return new WalletRepository(
		        okHttpClient, preferenceRepositoryType, accountKeystoreService, networkRepository);
	}

	@Singleton
	@Provides
	TransactionRepositoryType provideTransactionRepository(
			EthereumNetworkRepositoryType networkRepository,
			AccountKeystoreService accountKeystoreService,
			TransactionsNetworkClientType blockExplorerClient,
            TransactionLocalSource inDiskCache) {
		return new TransactionRepository(
				networkRepository,
				accountKeystoreService,
				inDiskCache,
				blockExplorerClient);
	}

	@Singleton
    @Provides
    TransactionLocalSource provideTransactionInDiskCache() {
        return new TransactionsRealmCache();
    }

	@Singleton
	@Provides
    TransactionsNetworkClientType provideBlockExplorerClient(
			OkHttpClient httpClient,
			Gson gson,
			EthereumNetworkRepositoryType ethereumNetworkRepository) {
		return new TransactionsNetworkClient(httpClient, gson, ethereumNetworkRepository);
	}

	@Singleton
    @Provides
    TokenRepositoryType provideTokenRepository(
            OkHttpClient okHttpClient,
            EthereumNetworkRepositoryType ethereumNetworkRepository,
            TokenExplorerClientType tokenExplorerClientType,
            TokenLocalSource tokenLocalSource,
            TransactionLocalSource inDiskCache) {
	    return new TokenRepository(
	            okHttpClient,
	            ethereumNetworkRepository,
	            tokenExplorerClientType,
                tokenLocalSource,
                inDiskCache);
    }

	@Singleton
    @Provides
    TokenExplorerClientType provideTokenService(OkHttpClient okHttpClient, Gson gson) {
	    return new EthplorerTokenService(okHttpClient, gson);
    }

    @Singleton
    @Provides
    TokenLocalSource provideRealmTokenSource() {
	    return new TokensRealmSource();
    }

    @Singleton
	@Provides
	GasSettingsRepositoryType provideGasSettingsRepository(EthereumNetworkRepositoryType ethereumNetworkRepository) {
		return new GasSettingsRepository(ethereumNetworkRepository);
	}
}
