package com.totsp.home.friday.control;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by rcooper on 7/10/15.
 */
public class CharAdapter extends XmlAdapter<String,Character> {

    @Override
    public String marshal(Character v) throws Exception {
        return new String(new char[]{v});
    }

    @Override
    public Character unmarshal(String v) throws Exception {
        if(v.length() == 1) {
            return v.charAt(0);
        } else if(v.length() == 0) {
            return ' ';
        } else {
            throw new RuntimeException("\""+v+"\" cannot be converted to a character");
        }
    }

}
