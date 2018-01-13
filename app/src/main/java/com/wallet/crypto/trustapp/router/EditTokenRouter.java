package com.wallet.crypto.trustapp.router;

import android.content.Context;
import android.content.Intent;

import com.wallet.crypto.trustapp.C;
import com.wallet.crypto.trustapp.ui.EditTokenActivity;

/**
 * Created by James on 12/01/2018.
 */

public class EditTokenRouter
{
    public void open(Context context, String address, String symbol, int decimals) {
        Intent intent = new Intent(context, EditTokenActivity.class);
        intent.putExtra(C.EXTRA_CONTRACT_ADDRESS, address);
        intent.putExtra(C.EXTRA_SYMBOL, symbol);
        intent.putExtra(C.EXTRA_DECIMALS, decimals);
        context.startActivity(intent);
    }
}