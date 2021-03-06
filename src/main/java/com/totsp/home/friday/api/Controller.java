package com.totsp.home.friday.api;

import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Created by rcooper on 7/10/15.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Controller {
    private String address;
    private ControllerType type;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ControllerType getType() {
        return type;
    }

    public void setType(ControllerType type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("address", address)
                .add("type", type)
                .toString();
    }
}
