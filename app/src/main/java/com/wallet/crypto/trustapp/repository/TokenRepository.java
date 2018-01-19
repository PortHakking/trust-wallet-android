package com.wallet.crypto.trustapp.repository;

import android.support.annotation.NonNull;
import android.util.Log;

import com.wallet.crypto.trustapp.entity.NetworkInfo;
import com.wallet.crypto.trustapp.entity.Token;
import com.wallet.crypto.trustapp.entity.TokenInfo;
import com.wallet.crypto.trustapp.entity.Wallet;
import com.wallet.crypto.trustapp.service.TokenExplorerClientType;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;
import org.web3j.abi.datatypes.Utf8String;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.OkHttpClient;

public class TokenRepository implements TokenRepositoryType {

    private final TokenExplorerClientType tokenNetworkService;
    private final TokenLocalSource tokenLocalSource;
    private final OkHttpClient httpClient;
    private final EthereumNetworkRepositoryType ethereumNetworkRepository;
    private Web3j web3j;

    public TokenRepository(
            OkHttpClient okHttpClient,
            EthereumNetworkRepositoryType ethereumNetworkRepository,
            TokenExplorerClientType tokenNetworkService,
            TokenLocalSource tokenLocalSource) {
        this.httpClient = okHttpClient;
        this.ethereumNetworkRepository = ethereumNetworkRepository;
        this.tokenNetworkService = tokenNetworkService;
        this.tokenLocalSource = tokenLocalSource;
        this.ethereumNetworkRepository.addOnChangeDefaultNetwork(this::buildWeb3jClient);
        buildWeb3jClient(ethereumNetworkRepository.getDefaultNetwork());
    }

    private void buildWeb3jClient(NetworkInfo defaultNetwork) {
        web3j = Web3jFactory.build(new HttpService(defaultNetwork.rpcServerUrl, httpClient, false));
    }

    @Override
    public Observable<Token[]> fetch(String walletAddress) {
        NetworkInfo defaultNetwork = ethereumNetworkRepository.getDefaultNetwork();
        Wallet wallet = new Wallet(walletAddress);
        return Single.merge(
                fetchTokensFromLocal(defaultNetwork, wallet),
                updateTokenInfoCache(defaultNetwork, wallet))
                .toObservable();
    }

    @Override
    public Observable<TokenInfo> update(String contractAddr) {
        return setupTokensFromLocal(contractAddr).toObservable();
    }

    private Single<TokenInfo> setupTokensFromLocal(String address)
    {
        return Single.fromCallable(() -> {
            try
            {
                TokenInfo result = new TokenInfo(
                        address,
                        getName(address),
                        getSymbol(address),
                        getDecimals(address));

                return result;
            }
            finally {

            }
        });
    }

    private Single<Token[]> fetchTokensFromLocal(NetworkInfo defaultNetwork, Wallet wallet) {
        return tokenLocalSource.fetch(defaultNetwork, wallet)
                .map(items -> getBalances(wallet, items));
    }

    private Token[] getBalances(Wallet wallet, TokenInfo[] items) {
        int len = items.length;
        Token[] result = new Token[len];
        for (int i = 0; i < len; i++) {
            BigDecimal balance = null;
            String name = null;
            try {
                balance = getBalance(wallet, items[i]);
            } catch (Exception e1) {
                Log.d("TOKEN", "Err", e1);
                                    /* Quietly */
            }
            System.out.println("NAME: " + name);
            result[i] = new Token(items[i], balance);
        }
        return result;
    }

    @Override
    public Completable addToken(Wallet wallet, String address, String symbol, int decimals) {
        return tokenLocalSource.put(
                ethereumNetworkRepository.getDefaultNetwork(),
                wallet,
                new TokenInfo(address, "", symbol, decimals));
    }

    @Override
    public Completable updateToken(Wallet wallet, String address) {
        return tokenLocalSource.update(
                ethereumNetworkRepository.getDefaultNetwork(),
                wallet,
                new TokenInfo(address, "", "", 0));
    }

    private Single<Token[]> updateTokenInfoCache(@NonNull NetworkInfo defaultNetwork, @NonNull Wallet wallet) {
        return Single.fromObservable(tokenNetworkService.fetch(wallet.address))
                .flatMap(tokenInfos -> tokenLocalSource.put(defaultNetwork, wallet, tokenInfos))
                .map(items -> getBalances(wallet, items));
    }

    private BigDecimal getBalance(Wallet wallet, TokenInfo tokenInfo) throws Exception {
        org.web3j.abi.datatypes.Function function = balanceOf(wallet.address);
        String responseValue = callSmartContractFunction(function, tokenInfo.address, wallet);

        List<Type> response = FunctionReturnDecoder.decode(
                responseValue, function.getOutputParameters());
        if (response.size() == 1) {
            return new BigDecimal(((Uint256) response.get(0)).getValue());
        } else {
            return null;
        }
    }

    private String getName(String address) throws Exception {
        org.web3j.abi.datatypes.Function function = nameOf();
        String responseValue = callSmartContractFunction(function, address, null);

        List<Type> response = FunctionReturnDecoder.decode(
                responseValue, function.getOutputParameters());
        if (response.size() == 1) {
            return (String)response.get(0).getValue();
        } else {
            return null;
        }
    }

    private String getSymbol(String address) throws Exception {
        org.web3j.abi.datatypes.Function function = symbolOf();
        String responseValue = callSmartContractFunction(function, address, null);

        List<Type> response = FunctionReturnDecoder.decode(
                responseValue, function.getOutputParameters());
        if (response.size() == 1) {
            return (String)response.get(0).getValue();
        } else {
            return null;
        }
    }

    private int getDecimals(String address) throws Exception {
        org.web3j.abi.datatypes.Function function = decimalsOf();
        String responseValue = callSmartContractFunction(function, address, null);

        List<Type> response = FunctionReturnDecoder.decode(
                responseValue, function.getOutputParameters());
        if (response.size() == 1) {
            return ((Uint8) response.get(0)).getValue().intValue();
        } else {
            return 18; //default
        }
    }

    private static org.web3j.abi.datatypes.Function balanceOf(String owner) {
        return new org.web3j.abi.datatypes.Function(
                "balanceOf",
                Collections.singletonList(new Address(owner)),
                Collections.singletonList(new TypeReference<Uint256>() {}));
    }

    private static org.web3j.abi.datatypes.Function nameOf() {
        return new Function("name",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
    }

    private static org.web3j.abi.datatypes.Function symbolOf() {
        return new Function("symbol",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
    }

    private static org.web3j.abi.datatypes.Function decimalsOf() {
        return new Function("decimals",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
    }

    private String callSmartContractFunction(
            org.web3j.abi.datatypes.Function function, String contractAddress, Wallet wallet) throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);
        String userAddr = (wallet == null ? null : wallet.address);

        org.web3j.protocol.core.methods.response.EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(userAddr, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST)
                .sendAsync().get();

        return response.getValue();
    }

    public static byte[] createTokenTransferData(String to, BigInteger tokenAmount) {
        List<Type> params = Arrays.asList(new Address(to), new Uint256(tokenAmount));

        List<TypeReference<?>> returnTypes = Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
        });

        Function function = new Function("transfer", params, returnTypes);
        String encodedFunction = FunctionEncoder.encode(function);
        return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction));
    }
}
