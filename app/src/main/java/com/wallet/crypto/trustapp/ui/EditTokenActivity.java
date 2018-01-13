package com.wallet.crypto.trustapp.ui;

/**
 * Created by James on 11/01/2018.
 */

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.wallet.crypto.trustapp.C;
import com.wallet.crypto.trustapp.R;
import com.wallet.crypto.trustapp.entity.Address;
import com.wallet.crypto.trustapp.entity.ErrorEnvelope;
import com.wallet.crypto.trustapp.viewmodel.EditTokenViewModel;
import com.wallet.crypto.trustapp.viewmodel.EditTokenViewModelFactory;
import com.wallet.crypto.trustapp.widget.SystemView;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class EditTokenActivity extends BaseActivity implements View.OnClickListener
{
    @Inject
    protected EditTokenViewModelFactory editTokenViewModelFactory;
    private EditTokenViewModel viewModel;

    private TextInputLayout addressLayout;
    private TextView address;
    private TextInputLayout symbolLayout;
    private TextView symbol;
    private TextInputLayout decimalsLayout;
    private TextView decimals;
    private SystemView systemView;
    private Dialog dialog;
    private int decimalValue;
    private String contractAddress;
    private String symbolValue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_token);

        toolbar();

        addressLayout = findViewById(R.id.address_input_layout);
        address = findViewById(R.id.address);
        symbolLayout = findViewById(R.id.symbol_input_layout);
        symbol = findViewById(R.id.symbol);
        decimalsLayout = findViewById(R.id.decimal_input_layout);
        decimals = findViewById(R.id.decimals);
        systemView = findViewById(R.id.system_view);
        systemView.hide();

        contractAddress = getIntent().getStringExtra(C.EXTRA_CONTRACT_ADDRESS);
        decimalValue = getIntent().getIntExtra(C.EXTRA_DECIMALS, C.ETHER_DECIMALS);
        symbolValue = getIntent().getStringExtra(C.EXTRA_SYMBOL);

        address.setText(contractAddress);
        decimals.setText(decimalValue);
        symbol.setText(symbolValue);

        findViewById(R.id.save).setOnClickListener(this);

        viewModel = ViewModelProviders.of(this, editTokenViewModelFactory)
                .get(EditTokenViewModel.class);
        viewModel.progress().observe(this, systemView::showProgress);
        viewModel.error().observe(this, this::onError);
        //viewModel.result().observe(this, this::onSaved);
    }

    private void onSaved(boolean result) {
        if (result) {
            //viewModel.showTokens(this);
            finish();
        }
    }

    private void onError(ErrorEnvelope errorEnvelope) {
        dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_dialog_error)
                .setMessage(R.string.error_add_token)
                .setPositiveButton(R.string.try_again, null)
                .create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save: {
                onSave();
            } break;
        }
    }

    private void onSave() {
        boolean isValid = true;
        String address = this.address.getText().toString();
        String symbol = this.symbol.getText().toString().toLowerCase();
        String rawDecimals = this.decimals.getText().toString();
        int decimals = 0;

        if (TextUtils.isEmpty(address)) {
            addressLayout.setError(getString(R.string.error_field_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(symbol)) {
            symbolLayout.setError(getString(R.string.error_field_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(rawDecimals)) {
            decimalsLayout.setError(getString(R.string.error_field_required));
            isValid = false;
        }

        try {
            decimals = Integer.valueOf(rawDecimals);
        } catch (NumberFormatException ex) {
            decimalsLayout.setError(getString(R.string.error_must_numeric));
            isValid = false;
        }

        if (!Address.isAddress(address)) {
            addressLayout.setError(getString(R.string.error_invalid_address));
            isValid = false;
        }

        if (isValid) {
            viewModel.save(address, symbol, decimals);
        }
    }
}
