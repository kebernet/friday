package com.totsp.home.friday.api;

import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Created by rcooper on 1/19/16.
 */
@XmlRootElement(name = "device")
@XmlAccessorType(XmlAccessType.PROPERTY)
public abstract class Device implements Serializable {

    private char houseCode;
    private int deviceCode;
    private State state;
    private DeviceType type;

    @XmlAttribute
    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type){
        this.type = type;
    }

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

    public void setDeviceCode(int value){
        this.deviceCode = value;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @XmlTransient
    public String getAddress() {
        return String.valueOf(houseCode) + String.valueOf(deviceCode);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        Device that = (Device) o;

        return Objects.equal(this.houseCode, that.houseCode) &&
                Objects.equal(this.deviceCode, that.deviceCode) &&
                Objects.equal(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(houseCode, deviceCode, type);
    }

    public boolean matches(char houseCode, int deviceCode){
        return this.getHouseCode() == houseCode && this.getDeviceCode() == deviceCode;
    }


    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("houseCode", houseCode)
                .add("deviceCode", deviceCode)
                .add("state", state)
                .add("type", type)
                .toString();
    }
}
