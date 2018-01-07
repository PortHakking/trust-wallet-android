package com.wallet.crypto.trustapp.util;

import java.math.BigDecimal;

/**
 * Created by justindg on 7/1/18.
 */

public class PriceUtils {
    private static PriceUtils instance = null;
    private static BigDecimal price = BigDecimal.ZERO;

    protected PriceUtils() {}

    public static PriceUtils getInstance() {
        if(instance == null) {
            instance = new PriceUtils();
        }
        return instance;
    }

    public static BigDecimal get() {
        return price;
    }

    public static void set(BigDecimal val) {
        price = val;
    }

}
