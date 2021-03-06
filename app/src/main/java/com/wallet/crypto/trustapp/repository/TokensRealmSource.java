package com.wallet.crypto.trustapp.repository;

import android.support.annotation.NonNull;

import com.wallet.crypto.trustapp.entity.NetworkInfo;
import com.wallet.crypto.trustapp.entity.TokenInfo;
import com.wallet.crypto.trustapp.entity.Wallet;
import com.wallet.crypto.trustapp.repository.entity.RealmTokenInfo;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.Sort;

public class TokensRealmSource implements TokenLocalSource {

    private final Map<String, RealmConfiguration> realmConfigurations = new HashMap<>();

    @Override
    public Completable put(NetworkInfo networkInfo, Wallet wallet, TokenInfo tokenInfo) {
        return Completable.fromAction(() -> putInNeed(networkInfo, wallet, tokenInfo));
    }

    @Override
    public Single<TokenInfo[]> put(NetworkInfo networkInfo, Wallet wallet, TokenInfo[] tokenInfos) {
        return Single.fromCallable(() -> {
            for (TokenInfo tokenInfo : tokenInfos) {
                putInNeed(networkInfo, wallet, tokenInfo);
            }
            return tokenInfos;
        })
        .flatMap(items -> fetch(networkInfo, wallet));
    }

    @Override
    public Single<TokenInfo[]> fetch(NetworkInfo networkInfo, Wallet wallet) {
        return Single.fromCallable(() -> {
            Realm realm = null;
            try {
                realm = getRealmInstance(networkInfo, wallet);
                RealmResults<RealmTokenInfo> realmItems = realm.where(RealmTokenInfo.class)
                        .sort("addedTime", Sort.ASCENDING)
                        .equalTo("isDisable", false)
                        .findAll();
                int len = realmItems.size();
                TokenInfo[] result = new TokenInfo[len];
                for (int i = 0; i < len; i++) {
                    RealmTokenInfo realmItem = realmItems.get(i);
                    if (realmItem != null) {
                        result[i] = new TokenInfo(
                                realmItem.getAddress(),
                                realmItem.getName(),
                                realmItem.getSymbol(),
                                realmItem.getDecimals());
                    }
                }
                return result;
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
        });
    }

    private void putInNeed(NetworkInfo networkInfo, Wallet wallet, TokenInfo tokenInfo) {
        Realm realm = null;
        try {
            realm = getRealmInstance(networkInfo, wallet);
            RealmTokenInfo realmTokenInfo = realm.where(RealmTokenInfo.class)
                    .equalTo("address", tokenInfo.address)
                    .findFirst();
            if (realmTokenInfo == null) {
                realm.executeTransaction(r -> {
                    RealmTokenInfo obj = r.createObject(RealmTokenInfo.class, tokenInfo.address);
                    obj.setName(tokenInfo.name);
                    obj.setSymbol(tokenInfo.symbol);
                    obj.setDecimals(tokenInfo.decimals);
                    obj.setAddedTime(System.currentTimeMillis());
                });
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private Realm getRealmInstance(NetworkInfo networkInfo, Wallet wallet) {
        String name = wallet.address + "_" + networkInfo.name + ".realm";
        RealmConfiguration config = realmConfigurations.get(name);
        if (config == null) {
            config = new RealmConfiguration.Builder()
                    .name(name)
                    .schemaVersion(2)
                    .migration(new TokenInfoMigration())
                    .build();
            realmConfigurations.put(name, config);
        }
        return Realm.getInstance(config);
    }

    private static class TokenInfoMigration implements RealmMigration {

        @Override
        public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
            RealmSchema schema = realm.getSchema();
            if (oldVersion == 1) {
                RealmObjectSchema tokenInfoSchema = schema.get("RealmTokenInfo");
                if (tokenInfoSchema != null) {
                    tokenInfoSchema.addField("isDisable", boolean.class);
                }
//                oldVersion++;
            }
        }
    }
}
