package com.wallet.crypto.trustapp.router;

import android.content.Context;
import android.content.Intent;

import com.wallet.crypto.trustapp.C;
import com.wallet.crypto.trustapp.ui.SendActivity;

import java.math.BigDecimal;

public class SendRouter {

    public void open(Context context, String symbol, double balance) {
        Intent intent = new Intent(context, SendActivity.class);
        intent.putExtra(C.EXTRA_SYMBOL, symbol);
        intent.putExtra(C.EXTRA_AMOUNT, balance);
        context.startActivity(intent);
    }
}
