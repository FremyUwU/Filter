package ru.shift;

import java.math.BigInteger;

public class IntStats {
    long count = 0;
    BigInteger min = null;
    BigInteger max = null;
    BigInteger sum = BigInteger.ZERO;

    public void accept(BigInteger val){
        count++;
        if(min == null || val.compareTo(min) < 0) min = val;
        if(max == null || val.compareTo(max) > 0) max = val;
        sum = sum.add(val);
    }
}
