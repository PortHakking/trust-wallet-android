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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.wallet.crypto.trustapp.C;
import com.wallet.crypto.trustapp.R;
import com.wallet.crypto.trustapp.ui.barcode.BarcodeCaptureActivity;
import com.wallet.crypto.trustapp.util.BalanceUtils;
import com.wallet.crypto.trustapp.util.PriceUtils;
import com.wallet.crypto.trustapp.util.QRURLParser;
import com.wallet.crypto.trustapp.viewmodel.SendViewModel;
import com.wallet.crypto.trustapp.viewmodel.SendViewModelFactory;

import org.ethereum.geth.Address;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class SendActivity extends BaseActivity {

    @Inject
    SendViewModelFactory sendViewModelFactory;
    SendViewModel viewModel;

    private static final int BARCODE_READER_REQUEST_CODE = 1;

    private EditText toAddressText;
    private EditText amountText;
    private EditText usdAmountText;
    private TextWatcher amountTextWatcher;
    private TextWatcher usdAmountTextWatcher;

    // In case we're sending tokens
    private boolean sendingTokens = false;
    private String contractAddress;
    private int decimals;
    private String symbol;
    private TextInputLayout toInputLayout;
    private TextInputLayout amountInputLayout;
    private TextInputLayout usdAmountInputLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_send);
        toolbar();

        viewModel = ViewModelProviders.of(this, sendViewModelFactory)
                .get(SendViewModel.class);

        toInputLayout = findViewById(R.id.to_input_layout);
        toAddressText = findViewById(R.id.send_to_address);
        amountInputLayout = findViewById(R.id.amount_input_layout);
        amountText = findViewById(R.id.send_amount);
        usdAmountInputLayout = findViewById(R.id.usd_amount_input_layout);
        usdAmountText = findViewById(R.id.usd_send_amount);

        initializeFieldListeners();

        contractAddress = getIntent().getStringExtra(C.EXTRA_CONTRACT_ADDRESS);
        decimals = getIntent().getIntExtra(C.EXTRA_DECIMALS, -1);
        symbol = getIntent().getStringExtra(C.EXTRA_SYMBOL);
        symbol = symbol == null ? C.ETH_SYMBOL : symbol;
        sendingTokens = getIntent().getBooleanExtra(C.EXTRA_SENDING_TOKENS, false);

        setTitle(getString(R.string.title_send) + " " + symbol);
        amountInputLayout.setHint(getString(R.string.hint_amount) + " (" + symbol + ")");
        usdAmountInputLayout.setHint(getString(R.string.hint_amount) + " (" + C.USD_SYMBOL + ")");

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
        amountText.setOnFocusChangeListener((view, b) -> {
            if (b) {
                amountText.addTextChangedListener(amountTextWatcher);
            } else {
                amountText.removeTextChangedListener(amountTextWatcher);
            }
        });

        usdAmountText.setOnFocusChangeListener((view, b) -> {
            if (b) {
                usdAmountText.addTextChangedListener(usdAmountTextWatcher);
            } else {
                usdAmountText.removeTextChangedListener(usdAmountTextWatcher);
            }
        });

        amountTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (charSequence.length() > 0) {
                        usdAmountText.setText(BalanceUtils.ethToUsd(PriceUtils.get().toString(), charSequence.toString()));
                    } else {
                        usdAmountText.getText().clear();
                    }
                } catch (NumberFormatException e) {
                    Log.e("SEND", e.getMessage(), e);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        usdAmountTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (charSequence.length() > 0) {
                        amountText.setText(BalanceUtils.usdToEth(charSequence.toString(), PriceUtils.get().toString()));
                    } else {
                        amountText.getText().clear();
                    }
                } catch (NumberFormatException e) {
                    Log.e("SEND", e.getMessage(), e);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
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

    private void onNext() {
        // Validate input fields
        boolean inputValid = true;
        final String to = toAddressText.getText().toString();
        if (!isAddressValid(to)) {
            toInputLayout.setError(getString(R.string.error_invalid_address));
            inputValid = false;
        }
        final String amount = amountText.getText().toString();
        if (!isValidEthAmount(amount)) {
            amountInputLayout.setError(getString(R.string.error_invalid_amount));
            inputValid = false;
        }

        if (!inputValid) {
            return;
        }

        viewModel.openConfirmation(this, to, amount, contractAddress, decimals, symbol, sendingTokens);
    }

    boolean isAddressValid(String address) {
        try {
            new Address(address);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    boolean isValidEthAmount(String eth) {
        try {
            String wei = BalanceUtils.EthToWei(eth);
            return wei != null;
        } catch (Exception e) {
            return false;
        }
    }
}
