package com.totsp.home.friday.x10;

/**
 * Created by rcooper on 6/30/15.
 */
public class X10Exception extends Exception {

    public X10Exception(String message){
        super(message);
    }

    public X10Exception(String message, Throwable cause){
        super(message, cause);
    }
}
