package me.add1.network;

import android.util.Pair;

import java.io.InputStream;
import java.util.Comparator;

/**
 * Created by dragonj on 15/11/19.
 */
public class ParamPair extends Pair<String, Object> implements Comparable<ParamPair>{
    public ParamPair(String name, InputStream value) {
        super(name, value);
    }

    public ParamPair(String name, int value) {
        super(name, value);
    }

    public ParamPair(String name, CharSequence value){
        super(name, value);
    }

    public ParamPair(String name, long value){
        super(name, value);
    }

    public ParamPair(String name, float value){
        super(name, value);
    }

    public ParamPair(String name, double value){
        super(name, value);
    }

    public ParamPair(String name, char value){
        super(name, value);
    }

    public ParamPair(String name, byte value){
        super(name, value);
    }

    public ParamPair(String name, Object value){
        super(name, value);
    }
    
    public String getName(){
        return first;
    }

    public Object getValue(){
        return second;
    }

    @Override
    public int compareTo(ParamPair another) {
        if (another == null)
            throw new RuntimeException("compare on a null object");
        return getName().compareTo(another.getName());
    }

}
