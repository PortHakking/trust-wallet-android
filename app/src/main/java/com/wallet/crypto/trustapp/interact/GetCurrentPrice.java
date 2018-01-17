package com.wallet.crypto.trustapp.interact;

import com.wallet.crypto.trustapp.repository.EthereumNetworkRepositoryType;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GetCurrentPrice {
    private final EthereumNetworkRepositoryType ethereumNetworkRepository;

    public GetCurrentPrice(EthereumNetworkRepositoryType ethereumNetworkRepository) {
        this.ethereumNetworkRepository = ethereumNetworkRepository;
    }

    public Single<String> getPrice() {
        final String[] price = new String[1];
        return ethereumNetworkRepository
                .getTicker()
                .observeOn(Schedulers.io())
                .flatMap(ticker -> {
                    if (ethereumNetworkRepository.getDefaultNetwork().isMainNetwork) {
                        price[0] = ticker.price;
                    }
                    return Single.just(price[0]);
                })
                .onErrorResumeNext(throwable -> Single.just(price[0]))
                .observeOn(AndroidSchedulers.mainThread());
    }
}
