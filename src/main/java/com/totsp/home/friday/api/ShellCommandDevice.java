package com.totsp.home.friday.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by rcooper on 1/19/16.
 */
@XmlRootElement(name="shell-command-device")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ShellCommandDevice extends Device {

    private String onCommand;
    private String offCommand;
    private String dimCommand;

    @XmlAttribute(name="dim-command")
    public String getDimCommand() {
        return dimCommand;
    }

    public void setDimCommand(String dimCommand) {
        this.dimCommand = dimCommand;
    }

    @XmlAttribute(name="off-command")
    public String getOffCommand() {
        return offCommand;
    }

    public void setOffCommand(String offCommand) {
        this.offCommand = offCommand;
    }

    @XmlAttribute(name = "on-command")
    public String getOnCommand() {
        return onCommand;
    }

    public void setOnCommand(String onCommand) {
        this.onCommand = onCommand;
    }


}
