package com.wallet.crypto.trustapp.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.wallet.crypto.trustapp.C;
import com.wallet.crypto.trustapp.R;
import com.wallet.crypto.trustapp.entity.NetworkInfo;
import com.wallet.crypto.trustapp.entity.Wallet;
import com.wallet.crypto.trustapp.ui.barcode.BarcodeCaptureActivity;
import com.wallet.crypto.trustapp.util.BalanceUtils;
import com.wallet.crypto.trustapp.util.PriceUtils;
import com.wallet.crypto.trustapp.util.QRURLParser;
import com.wallet.crypto.trustapp.viewmodel.SendViewModel;
import com.wallet.crypto.trustapp.viewmodel.SendViewModelFactory;

import org.ethereum.geth.Address;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

import static com.wallet.crypto.trustapp.C.ETHEREUM_NETWORK_NAME;

public class SendActivity extends BaseActivity {

    @Inject
    SendViewModelFactory sendViewModelFactory;
    SendViewModel viewModel;

    private static final int BARCODE_READER_REQUEST_CODE = 1;

    private EditText toAddressText;
    private EditText amountText;
    private EditText usdAmountText;

    // In case we're sending tokens
    private boolean sendingTokens = false;
    private String contractAddress;
    private int decimals;
    private String symbol;
    private TextInputLayout toInputLayout;
    private TextInputLayout amountInputLayout;
    private TextInputLayout usdAmountInputLayout;
    private TextView ethBalanceText;
    private TextView usdBalanceText;

    private String ethBalance;
    private String usdBalance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_send);
        toolbar();

        viewModel = ViewModelProviders.of(this, sendViewModelFactory)
                .get(SendViewModel.class);
        viewModel.defaultWalletBalance().observe(this, this::onBalanceChanged);
        viewModel.defaultNetwork().observe(this, this::onDefaultNetwork);

        toInputLayout = findViewById(R.id.to_input_layout);
        toAddressText = findViewById(R.id.send_to_address);
        amountInputLayout = findViewById(R.id.amount_input_layout);
        amountText = findViewById(R.id.send_amount);
        usdAmountInputLayout = findViewById(R.id.usd_amount_input_layout);
        usdAmountText = findViewById(R.id.usd_send_amount);
        ethBalanceText = findViewById(R.id.eth_balance_text);
        usdBalanceText = findViewById(R.id.usd_balance_text);

        initializeFieldListeners();

        contractAddress = getIntent().getStringExtra(C.EXTRA_CONTRACT_ADDRESS);
        decimals = getIntent().getIntExtra(C.EXTRA_DECIMALS, C.ETHER_DECIMALS);
        symbol = getIntent().getStringExtra(C.EXTRA_SYMBOL);
        symbol = symbol == null ? C.ETH_SYMBOL : symbol;
        sendingTokens = getIntent().getBooleanExtra(C.EXTRA_SENDING_TOKENS, false);

        setTitle(getString(R.string.title_send) + " " + symbol);
        amountInputLayout.setHint(getString(R.string.hint_amount_with_symbol, symbol));
        usdAmountInputLayout.setHint(getString(R.string.hint_amount_with_symbol, C.USD_SYMBOL));

        // Populate to address if it has been passed forward
        String toAddress = getIntent().getStringExtra(C.EXTRA_ADDRESS);
        if (toAddress != null) {
            toAddressText.setText(toAddress);
        }

        ImageButton scanBarcodeButton = findViewById(R.id.scan_barcode_button);
        scanBarcodeButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
            startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
        });
    }

    private void initializeFieldListeners() {
        amountText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                amountText.addTextChangedListener(amountTextWatcher);
            } else {
                amountText.removeTextChangedListener(amountTextWatcher);
            }
        });

        usdAmountText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                usdAmountText.addTextChangedListener(usdAmountTextWatcher);
            } else {
                usdAmountText.removeTextChangedListener(usdAmountTextWatcher);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next: {
                onNext();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

                    QRURLParser parser = QRURLParser.getInstance();
                    String extracted_address = parser.extractAddressFromQrString(barcode.displayValue);
                    if (extracted_address == null) {
                        Toast.makeText(this, R.string.toast_qr_code_no_address, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Point[] p = barcode.cornerPoints;
                    toAddressText.setText(extracted_address);
                }
            } else {
                Log.e("SEND", String.format(getString(R.string.barcode_error_format),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ethBalanceText.setText(getString(R.string.unknown_balance_without_symbol));
        viewModel.prepare();
    }

    private void onBalanceChanged(Map<String, String> balance) {
        NetworkInfo networkInfo = viewModel.defaultNetwork().getValue();
        Wallet wallet = viewModel.defaultWallet().getValue();
        if (networkInfo == null || wallet == null) {
            return;
        }

        ethBalance = balance.get(networkInfo.symbol);
        ethBalanceText.setText(ethBalance + " " + networkInfo.symbol);

        usdBalance = balance.get(C.USD_SYMBOL);
        usdBalanceText.setText(C.USD_SYMBOL + usdBalance);
    }

    private void onDefaultNetwork(NetworkInfo networkInfo) {
        if (networkInfo != null && networkInfo.name.equals(ETHEREUM_NETWORK_NAME)) {
            usdAmountInputLayout.setVisibility(View.VISIBLE);
            usdBalanceText.setVisibility(View.VISIBLE);
        }
    }

    private void onNext() {
        // Validate input fields
        boolean inputValid = true;
        final String to = toAddressText.getText().toString();
        if (!isAddressValid(to)) {
            toInputLayout.setError(getString(R.string.error_invalid_address));
            inputValid = false;
        }
        final String amount = amountText.getText().toString();
        if (!isValidAmount(amount)) {
            amountInputLayout.setError(getString(R.string.error_invalid_amount));
            inputValid = false;
        } else if (!isAvailableAmount(amount)) {
            amountInputLayout.setError(getString(R.string.error_unavailable_amount));
            inputValid = false;
        } else {
            amountInputLayout.setErrorEnabled(false);
        }

        if (!inputValid) {
            return;
        }

        toInputLayout.setErrorEnabled(false);
        amountInputLayout.setErrorEnabled(false);

        BigInteger amountInSubunits = BalanceUtils.baseToSubunit(amount, decimals);
        viewModel.openConfirmation(this, to, amountInSubunits, contractAddress, decimals, symbol, sendingTokens);
    }

    boolean isAvailableAmount(String amount) {
        if (amount.length() > 0 && ethBalance != null) {
            BigDecimal amountVal = new BigDecimal(amount);
            return amountVal.compareTo(new BigDecimal(ethBalance)) != 1;
        }
        return false;
    }

    boolean isAddressValid(String address) {
        try {
            new Address(address);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    boolean isValidAmount(String eth) {
        try {
            String wei = BalanceUtils.EthToWei(eth);
            return wei != null;
        } catch (Exception e) {
            return false;
        }
    }

    private final TextWatcher amountTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (charSequence.length() > 0 && !charSequence.toString().equals(getString(R.string.decimal_point))) {
                usdAmountText.setText(BalanceUtils.ethToUsd(PriceUtils.get().toString(), charSequence.toString()));
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length() > 0 && !editable.toString().equals(getString(R.string.decimal_point))) {
                if (!isAvailableAmount(editable.toString())) {
                    amountInputLayout.setError(getString(R.string.error_unavailable_amount));
                } else {
                    amountInputLayout.setErrorEnabled(false);
                }
            } else {
                amountInputLayout.setErrorEnabled(false);
            }
        }
    };

    private final TextWatcher usdAmountTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (charSequence.length() > 0 && !charSequence.toString().equals(getString(R.string.decimal_point))) {
                amountText.setText(BalanceUtils.usdToEth(charSequence.toString(), PriceUtils.get().toString()));
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };
}
