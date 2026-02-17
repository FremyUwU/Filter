package ru.shift;

public class StringStats {
    long count = 0;
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;

    public void accept(String s){
        count++;
        int len = s.length();
        if(min > len) min = len;
        if(max < len) max = len;
    }
}
