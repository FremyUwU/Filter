package ru.shift;

public class FloatStats {
    long count =0;
    Double min = null;
    Double max = null;
    double sum = 0;

    void accept(double val){
        count++;
        if(min == null || val < min) min = val;
        if(max == null || val > max) max = val;
        sum+=val;
    }
}
