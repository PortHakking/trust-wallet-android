package com.wallet.crypto.trustapp.ui.widget.holder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wallet.crypto.trustapp.R;
import com.wallet.crypto.trustapp.entity.Token;
import com.wallet.crypto.trustapp.ui.widget.OnTokenClickListener;

import java.math.BigDecimal;

public class TokenHolder extends BinderViewHolder<Token> implements View.OnClickListener, View.OnLongClickListener {

    private final TextView symbol;
    private final TextView balance;

    private Token token;
    private OnTokenClickListener onTokenClickListener;

    public TokenHolder(int resId, ViewGroup parent) {
        super(resId, parent);

        symbol = findViewById(R.id.symbol);
        balance = findViewById(R.id.balance);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    @Override
    public void bind(@Nullable Token data, @NonNull Bundle addition) {
        this.token = data;
        if (data == null) {
            fillEmpty();
            return;
        }
        try {
            symbol.setText(token.tokenInfo.symbol);
            setBalance(token.tokenInfo.decimals);
        } catch (Exception e) {
            //try to work out what the minimum value is
            if (!token.balance.equals(BigDecimal.ZERO)) {
                calculateMinimumScalingFactor();
            }
            else {
                fillEmpty();
            }
        }
    }

    private void setBalance(int decimals) throws Exception
    {
        BigDecimal decimalDivisor = new BigDecimal(Math.pow(10, decimals));
        BigDecimal ethBalance = decimals > 0
                ? token.balance.divide(decimalDivisor) : token.balance;
        String value = ethBalance.compareTo(BigDecimal.ZERO) == 0
                ? "0"
                : ethBalance.toPlainString();
        this.balance.setText(value);
    }

    private void calculateMinimumScalingFactor() {
        try
        {
            double balanceDb = token.balance.doubleValue();
            int scaleFactor = (int) Math.log10(balanceDb) - 1;
            //recommended scaling factor is about 1 less than this value
            //TODO: easy: auto-correct user error, don't display a null when there are tokens: DONE
            //TODO: hard - pull decimals from contract
            setBalance(scaleFactor);
        }
        catch (Exception e) {
            //still can't display - default to displaying the '-'
            fillEmpty();
        }
    }

    private void fillEmpty() {
        balance.setText(R.string.NA);
        balance.setText(R.string.minus);
    }

    @Override
    public void onClick(View v) {
        if (onTokenClickListener != null) {
            onTokenClickListener.onTokenClick(v, token);
        }
    }

    public void setOnTokenClickListener(OnTokenClickListener onTokenClickListener) {
        this.onTokenClickListener = onTokenClickListener;
    }

    @Override
    public boolean onLongClick(View v)
    {
        if (onTokenClickListener != null) {
            onTokenClickListener.onTokenLongClick(v, token);
        }
        return false;
    }
}
