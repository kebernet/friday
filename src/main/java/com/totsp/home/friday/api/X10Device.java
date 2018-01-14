package com.totsp.home.friday.api;

import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by rcooper on 6/29/15.
 */
@XmlRootElement(name = "x10-device")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class X10Device extends Device {
    private String controllerAddress;
    private String name;
    private boolean useOnOff = false;

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


    @XmlAttribute(name="use-on-off")
    public boolean isUseOnOff(){
        return useOnOff;
    }

    public void setUseOnOff(boolean onOff){
        this.useOnOff = onOff;
    }


    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("controllerAddress", controllerAddress)
                .add("deviceCode", getDeviceCode())
                .add("houseCode", getHouseCode())
                .add("state", getState())
                .add("type", getType())
                .add("useOnOff", isUseOnOff())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        X10Device x10Device = (X10Device) o;
        return Objects.equal(getControllerAddress(), x10Device.getControllerAddress()) &&
                getType() == x10Device.getType() &&
                Objects.equal(getName(), x10Device.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), getControllerAddress(), getType(), getName());
    }
}
