package com.totsp.home.friday.x10;

import com.totsp.home.friday.api.Device;

/**
 * Created by rcooper on 7/16/15.
 */
public class CM19A extends AbstractMochadInterface {
    public CM19A(UnitEventDispatcher dispatcher, String mochadHost, int mochadPort) throws X10Exception {
        super(dispatcher, mochadHost, mochadPort);
    }

    @Override
    public void addCommand(Device device, Command command) {
        command.setUseRadioFrequency(true);
        double level = ((double) command.getLevel()) / 100d;
        level = level * 9;
        if(command.getFunctionByte() == Command.BRIGHT || command.getFunctionByte() == Command.DIM){
            command.setFunction(command.getFunctionByte(), 5);
            Command newC = new Command(command.getHouseCode()+""+command.getUnitCode(), Command.ON);
            newC.setUseRadioFrequency(true);
            if(device.getState() == null || !device.getState().isOn()) {
                queue.add(newC);
            }
            for(double i=0; i < level; i+= 1){
                newC = new Command(command.getHouseCode()+""+command.getUnitCode(), command.getFunctionByte(), 20);
                newC.setUseRadioFrequency(true);
                queue.add(newC);
            }
        } else {
            this.queue.add(command);
        }
    }
}
