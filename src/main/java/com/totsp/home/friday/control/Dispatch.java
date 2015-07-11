package com.totsp.home.friday.control;

import com.google.inject.Singleton;
import com.totsp.home.friday.api.Device;
import com.totsp.home.friday.api.DeviceType;
import com.totsp.home.friday.api.State;
import com.totsp.home.friday.x10.Command;
import com.totsp.home.friday.x10.X10Interface;
import com.totsp.home.friday.x10.UnitEvent;
import com.totsp.home.friday.x10.UnitListener;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by rcooper on 6/29/15.
 */
@Singleton
public class Dispatch {
    private static final Logger LOGGER = Logger.getLogger(Dispatch.class.getCanonicalName());
    private Map<String, Device> devices = new ConcurrentHashMap<>();
    private Map<String, X10Interface> controllers = new ConcurrentHashMap<>();
    private Map<Device, X10Interface> controllerMap = new ConcurrentHashMap<>();
    private Map<Device, UnitListener> listenerMap = new ConcurrentHashMap<>();
    private List<DeviceStateChangedListener> stateListeners = new CopyOnWriteArrayList<>();

    public Device findDevice(String address){
        return devices.get(address);
    }

    public void transition(String address, State state){
        Device device = findDevice(address);
        State current = device.getState();
        X10Interface controller = controllerMap.get(device);
        if(current == null){
            controller.addCommand(new Command(address, Command.OFF));
            controller.addCommand(new Command(address, Command.ON));
            current = new State(true, 100);
        }
        Command update;
        switch(device.getType()) {
            case LIGHT:
                if(state.isOn() != current.isOn()) {
                    update = new Command(address, state.isOn() ? Command.BRIGHT : Command.DIM, 100);
                } else {
                    int diff = current.getBrightness() - state.getBrightness();
                    update = new Command(address, diff > 0 ? Command.DIM : Command.BRIGHT, Math.abs(diff));
                }
                break;
            default:
                update = new Command(address, state.isOn() ? Command.ON : Command.OFF);
        }
        controller.addCommand(update);
        device.setState(state);
    }


    public void addDevice(final Device device, X10Interface controller){
        this.devices.put(device.getAddress(), device);
        this.controllerMap.put(device, controller);
        UnitListener listener = new UnitListener() {
            @Override
            public void allUnitsOff(UnitEvent event) {
                if(device.getState() == null || device.getState().isOn()){
                    LOGGER.info("Got ALL UNITS OFF for "+device.getAddress());
                    State old = device.getState();
                    State state = new State(false, 0);
                    device.setState(state);
                    dispatchEvent(device, old);
                }
            }

            @Override
            public void allLightsOff(UnitEvent event) {
                if(device.getType() == DeviceType.LIGHT){
                    if(device.getState() == null || device.getState().isOn()){
                        LOGGER.info("Got ALL LIGHTS OFF for "+device.getAddress());
                        State old = device.getState();
                        State state = new State(false, 0);
                        device.setState(state);
                        dispatchEvent(device, old);
                    }
                }
            }

            @Override
            public void allLightsOn(UnitEvent event) {
                if(device.getType() == DeviceType.LIGHT){
                    if(device.getState() == null || !device.getState().isOn()){
                        LOGGER.info("Got ALL LIGHTS ON for "+device.getAddress());
                        State old = device.getState();
                        State state = new State(true, 100);
                        device.setState(state);
                        dispatchEvent(device, old);
                    }
                }
            }

            @Override
            public void unitOn(UnitEvent event) {
                if(device.matches(event.getCommand().getHouseCode(), event.getCommand().getUnitCode())){
                    if(device.getState() == null || !device.getState().isOn()){
                        LOGGER.info("Got ON for "+device.getAddress());
                        State old = device.getState();
                        State state = new State(true, 100);
                        device.setState(state);
                        dispatchEvent(device, state);
                    }
                }
            }

            @Override
            public void unitOff(UnitEvent event) {
                if(device.matches(event.getCommand().getHouseCode(), event.getCommand().getUnitCode())){
                    if(device.getState() == null || device.getState().isOn()){
                        LOGGER.info("Got OFF for "+device.getAddress());
                        State old = device.getState();
                        State state = new State(false, 0);
                        device.setState(state);
                        dispatchEvent(device, state);
                    }
                }
            }

            @Override
            public void unitDim(UnitEvent event) {
                if(device.matches(event.getCommand().getHouseCode(), event.getCommand().getUnitCode())){
                    if(device.getState() == null || device.getState().isOn()){
                        State old = device.getState();
                        State state = new State(true, (old == null ? 100 : old.getBrightness() ) - event.getCommand().getLevel() );
                        device.setState(state);
                        dispatchEvent(device, state);
                    }
                }

            }

            @Override
            public void unitBright(UnitEvent event) {
                if(device.matches(event.getCommand().getHouseCode(), event.getCommand().getUnitCode())){
                    if(device.getState() == null || device.getState().isOn()){
                        State old = device.getState();
                        State state = new State(true, (old == null ? 0 : old.getBrightness() ) + event.getCommand().getLevel() );
                        device.setState(state);
                        dispatchEvent(device, state);
                    }
                }
            }
        };
    }

    private void dispatchEvent(Device device, State previous){
        for(DeviceStateChangedListener l : this.stateListeners){
            try {
                l.onStateChanged(device, previous);
            } catch(Exception e){
                LOGGER.log(Level.WARNING, "Exception dispatching event for "+device.getAddress(), e);
            }
        }
    }


    public String dumpState(){
        StringBuilder builder = new StringBuilder();
        for(Device device : devices.values()){
            builder = builder.append(device.getAddress())
                .append(" (")
                .append(device.getType())
                .append(") ");
            if(device.getState() == null){
                builder = builder.append("no data");
            } else {
                builder = builder.append(device.getState().isOn() ? "on " : "off");
                if(device.getType() == DeviceType.LIGHT){
                    builder = builder.append(device.getState().getBrightness());
                }
            }
            builder = builder.append("\n");
        }
        return builder.toString();
    }

    public Collection<Device> devices() {
        return this.devices.values();
    }


    public interface DeviceStateChangedListener {

        void onStateChanged(Device device, State previous);
    }


}
