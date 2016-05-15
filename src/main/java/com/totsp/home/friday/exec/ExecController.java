package com.totsp.home.friday.exec;

import com.google.common.collect.Lists;
import com.totsp.home.friday.api.Device;
import com.totsp.home.friday.driver.ControlInterface;
import com.totsp.home.friday.x10.Command;
import com.totsp.home.friday.x10.UnitListener;

import java.util.LinkedList;

/**
 * Created by rcooper on 1/19/16.
 */
public class ExecController implements ControlInterface {

    private LinkedList<UnitListener> listeners = Lists.newLinkedList();

    @Override
    public void addUnitListener(UnitListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeUnitListener(UnitListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void addCommand(Device device, Command command) {

    }
}
