package com.totsp.home.friday.api;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by rcooper on 7/10/15.
 */
@XmlRootElement(name="configuration")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Configuration {

    private List<Controller> controllers = Lists.newArrayList();
    private List<Device> devices = Lists.newArrayList();

    @XmlElement(name="controller")
    @XmlElementWrapper(name="controllers")
    public List<Controller> getControllers() {
        return controllers;
    }

    public void setControllers(List<Controller> controlers) {
        this.controllers = controlers;
    }

    @XmlElements({
            @XmlElement(name = "x10-device", type = X10Device.class),
            @XmlElement(name = "shell-command-device", type = ShellCommandDevice.class)
    })
    @XmlElementWrapper( name="devices" )
    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configuration that = (Configuration) o;
        return Objects.equal(controllers, that.controllers) &&
                Objects.equal(devices, that.devices);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(controllers, devices);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("controllers", controllers)
                .add("devices", devices)
                .toString();
    }
}
