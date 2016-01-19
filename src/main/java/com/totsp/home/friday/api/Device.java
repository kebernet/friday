package com.totsp.home.friday.api;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

/**
 * Created by rcooper on 1/19/16.
 */
public abstract class Device implements Serializable {

    protected char houseCode;
    protected int deviceCode;

    @XmlAttribute(name="house")
    public char getHouseCode() {
        return houseCode;
    }

    public void setHouseCode(char houseCode) {
        this.houseCode = houseCode;
    }

    @XmlAttribute(name="device")
    public int getDeviceCode() {
        return deviceCode;
    }
}
