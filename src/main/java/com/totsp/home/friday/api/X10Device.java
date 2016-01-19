package com.totsp.home.friday.api;

import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Created by rcooper on 6/29/15.
 */
@XmlRootElement(name = "x10-device")
public class X10Device extends Device {
    private String controllerAddress;
    private State state;
    private DeviceType type;
    private String name;



    public void setDeviceCode(int deviceCode) {
        this.deviceCode = deviceCode;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @XmlAttribute
    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type){
        this.type = type;
    }

    public boolean matches(char houseCode, int deviceCode){
        return this.houseCode == houseCode && this.deviceCode == deviceCode;
    }

    @XmlAttribute(name="controller-address")
    public String getControllerAddress() {
        return controllerAddress;
    }

    public void setControllerAddress(String controllerAddress) {
        this.controllerAddress = controllerAddress;
    }

    @XmlAttribute(name="name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public String getAddress() {
        return String.valueOf(houseCode) + String.valueOf(deviceCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        X10Device x10Device = (X10Device) o;
        return Objects.equal(houseCode, x10Device.houseCode) &&
                Objects.equal(deviceCode, x10Device.deviceCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(houseCode, deviceCode);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("controllerAddress", controllerAddress)
                .add("deviceCode", deviceCode)
                .add("houseCode", houseCode)
                .add("state", state)
                .add("type", type)
                .toString();
    }
}
