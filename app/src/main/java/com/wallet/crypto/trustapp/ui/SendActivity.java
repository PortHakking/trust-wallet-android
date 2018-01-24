package com.wallet.crypto.trustapp.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.wallet.crypto.trustapp.C;
import com.wallet.crypto.trustapp.R;
import com.wallet.crypto.trustapp.entity.GasSettings;
import com.wallet.crypto.trustapp.ui.barcode.BarcodeCaptureActivity;
import com.wallet.crypto.trustapp.util.BalanceUtils;
import com.wallet.crypto.trustapp.util.QRURLParser;
import com.wallet.crypto.trustapp.viewmodel.SendViewModel;
import com.wallet.crypto.trustapp.viewmodel.SendViewModelFactory;

import org.ethereum.geth.Address;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class SendActivity extends BaseActivity {

    @Inject
    SendViewModelFactory sendViewModelFactory;
    SendViewModel viewModel;

    private static final int BARCODE_READER_REQUEST_CODE = 1;

    private EditText toAddressText;
    private EditText amountText;

    // In case we're sending tokens
    private boolean sendingTokens = false;
    private String contractAddress;
    private int decimals;
    private double balance;
    private String symbol;
    private TextInputLayout toInputLayout;
    private TextInputLayout amountInputLayout;
    private TextView viewBalance;
    private Button addAll;

    private boolean confirmationForTokenTransfer = false;
    private double networkFee = 0;

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
        viewBalance = findViewById(R.id.textViewBalance);
        addAll = findViewById(R.id.buttonAll);

        contractAddress = getIntent().getStringExtra(C.EXTRA_CONTRACT_ADDRESS);
        decimals = getIntent().getIntExtra(C.EXTRA_DECIMALS, C.ETHER_DECIMALS);
        balance = getIntent().getDoubleExtra(C.EXTRA_AMOUNT, 0);
        symbol = getIntent().getStringExtra(C.EXTRA_SYMBOL);
        symbol = symbol == null ? C.ETH_SYMBOL : symbol;
        sendingTokens = getIntent().getBooleanExtra(C.EXTRA_SENDING_TOKENS, false);

        String balanceStatement = String.valueOf(balance) + " " + symbol;
        viewBalance.setText(balanceStatement);

        setTitle(getString(R.string.title_send) + " " + symbol);
        amountInputLayout.setHint(getString(R.string.hint_amount) + " " + symbol);

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

        viewModel.gasSettings().observe(this, this::onGasSettings);
        addAll.setOnClickListener(view -> addMax());
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
    protected void onResume() {
        super.onResume();
        viewModel.prepare(confirmationForTokenTransfer);
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

    private void onGasSettings(GasSettings gasSettings) {
        networkFee = BalanceUtils.weiToEth(gasSettings
                .gasPrice.multiply(gasSettings.gasLimit)).doubleValue();

        DecimalFormat format = new DecimalFormat("###.0000#");

        double maxTransfer = balance - networkFee;
        String balanceStatement = "Max " + format.format(maxTransfer) + " " + symbol;
        viewBalance.setText(balanceStatement);
    }

    private void addMax() {
        double maxTransfer = balance - networkFee;
        amountText.setText(String.valueOf(maxTransfer));
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
        }

        if (!inputValid) {
            return;
        }

        toInputLayout.setErrorEnabled(false);
        amountInputLayout.setErrorEnabled(false);

        BigInteger amountInSubunits = BalanceUtils.baseToSubunit(amount, decimals);
        viewModel.openConfirmation(this, to, amountInSubunits, contractAddress, decimals, symbol, sendingTokens);
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
}
