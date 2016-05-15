package com.totsp.home.friday.x10;

import com.totsp.home.friday.api.Device;

/**
 * Created by rcooper on 8/31/15.
 */
public class CM15A extends AbstractMochadInterface {

    public CM15A(UnitEventDispatcher dispatcher, String mochadHost, int mochadPort) throws X10Exception {
        super(dispatcher, mochadHost, mochadPort);
    }

    @Override
    public void addCommand(Device device, Command command) {
        this.queue.add(command);
    }
}
