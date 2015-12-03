package me.add1.network;

import android.util.Pair;

/**
 * Created by dragonj on 15/11/18.
 */
public class NameValuePair extends Pair<String, String> {

    public NameValuePair(String first, String second) {
        super(first, second);
    }

    public NameValuePair(String first, long second) {
        super(first, String.valueOf(second));
    }

    public NameValuePair(String first, int second) {
        super(first, String.valueOf(second));
    }

    public NameValuePair(String first, float second) {
        super(first, String.valueOf(second));
    }

    public NameValuePair(String first, double second) {
        super(first, String.valueOf(second));
    }

    public String getName() {
        return first;
    }

    public String getValue() {
        return second;
    }
}
