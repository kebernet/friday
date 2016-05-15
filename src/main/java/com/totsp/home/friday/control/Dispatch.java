package com.totsp.home.friday.control;

import com.google.inject.Singleton;
import com.totsp.home.friday.api.Device;
import com.totsp.home.friday.api.DeviceType;
import com.totsp.home.friday.api.State;
import com.totsp.home.friday.driver.ControlInterface;
import com.totsp.home.friday.x10.Command;
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
    private Map<String, ControlInterface> controllers = new ConcurrentHashMap<>();
    private Map<Device, ControlInterface> controllerMap = new ConcurrentHashMap<>();
    private Map<Device, UnitListener> listenerMap = new ConcurrentHashMap<>();
    private List<DeviceStateChangedListener> stateListeners = new CopyOnWriteArrayList<>();

    public Device findDevice(String address){
        return devices.get(address);
    }

    public void transition(String address, State state){
        Device device = findDevice(address);
        State current = device.getState();
        ControlInterface controller = controllerMap.get(device);
        if(controller == null){
            LOGGER.info(""+controllerMap);
            throw new RuntimeException("Controller for "+device+" not found!");
        }
        if(current == null){
            controller.addCommand(device, new Command(address, Command.OFF));
            controller.addCommand(device, new Command(address, Command.ON));
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
        controller.addCommand(device, update);
        device.setState(state);
    }


    public void addDevice(final Device x10Device, ControlInterface controller){
        this.devices.put(x10Device.getAddress(), x10Device);
        this.controllerMap.put(x10Device, controller);
        UnitListener listener = new UnitListener() {
            @Override
            public void allUnitsOff(UnitEvent event) {
                if(x10Device.getState() == null || x10Device.getState().isOn()){
                    LOGGER.info("Got ALL UNITS OFF for "+ x10Device.getAddress());
                    State old = x10Device.getState();
                    State state = new State(false, 0);
                    x10Device.setState(state);
                    dispatchEvent(x10Device, old);
                }
            }

            @Override
            public void allLightsOff(UnitEvent event) {
                if(x10Device.getType() == DeviceType.LIGHT){
                    if(x10Device.getState() == null || x10Device.getState().isOn()){
                        LOGGER.info("Got ALL LIGHTS OFF for "+ x10Device.getAddress());
                        State old = x10Device.getState();
                        State state = new State(false, 0);
                        x10Device.setState(state);
                        dispatchEvent(x10Device, old);
                    }
                }
            }

            @Override
            public void allLightsOn(UnitEvent event) {
                if(x10Device.getType() == DeviceType.LIGHT){
                    if(x10Device.getState() == null || !x10Device.getState().isOn()){
                        LOGGER.info("Got ALL LIGHTS ON for "+ x10Device.getAddress());
                        State old = x10Device.getState();
                        State state = new State(true, 100);
                        x10Device.setState(state);
                        dispatchEvent(x10Device, old);
                    }
                }
            }

            @Override
            public void unitOn(UnitEvent event) {
                if(x10Device.matches(event.getCommand().getHouseCode(), event.getCommand().getUnitCode())){
                    if(x10Device.getState() == null || !x10Device.getState().isOn()){
                        LOGGER.info("Got ON for "+ x10Device.getAddress());
                        State old = x10Device.getState();
                        State state = new State(true, 100);
                        x10Device.setState(state);
                        dispatchEvent(x10Device, state);
                    }
                }
            }

            @Override
            public void unitOff(UnitEvent event) {
                if(x10Device.matches(event.getCommand().getHouseCode(), event.getCommand().getUnitCode())){
                    if(x10Device.getState() == null || x10Device.getState().isOn()){
                        LOGGER.info("Got OFF for "+ x10Device.getAddress());
                        State old = x10Device.getState();
                        State state = new State(false, 0);
                        x10Device.setState(state);
                        dispatchEvent(x10Device, state);
                    }
                }
            }

            @Override
            public void unitDim(UnitEvent event) {
                if(x10Device.matches(event.getCommand().getHouseCode(), event.getCommand().getUnitCode())){
                    if(x10Device.getState() == null || x10Device.getState().isOn()){
                        State old = x10Device.getState();
                        State state = new State(true, (old == null ? 100 : old.getBrightness() ) - event.getCommand().getLevel() );
                        x10Device.setState(state);
                        dispatchEvent(x10Device, state);
                    }
                }

            }

            @Override
            public void unitBright(UnitEvent event) {
                if(x10Device.matches(event.getCommand().getHouseCode(), event.getCommand().getUnitCode())){
                    if(x10Device.getState() == null || x10Device.getState().isOn()){
                        State old = x10Device.getState();
                        State state = new State(true, (old == null ? 0 : old.getBrightness() ) + event.getCommand().getLevel() );
                        x10Device.setState(state);
                        dispatchEvent(x10Device, state);
                    }
                }
            }
        };
        listenerMap.put(x10Device, listener);
    }

    private void dispatchEvent(Device x10Device, State previous){
        for(DeviceStateChangedListener l : this.stateListeners){
            try {
                l.onStateChanged(x10Device, previous);
            } catch(Exception e){
                LOGGER.log(Level.WARNING, "Exception dispatching event for "+ x10Device.getAddress(), e);
            }
        }
    }


    public String dumpState(){
        StringBuilder builder = new StringBuilder();
        for(Device x10Device : devices.values()){
            builder = builder.append(x10Device.getAddress())
                .append(" (")
                .append(x10Device.getType())
                .append(") ");
            if(x10Device.getState() == null){
                builder = builder.append("no data");
            } else {
                builder = builder.append(x10Device.getState().isOn() ? "on " : "off");
                if(x10Device.getType() == DeviceType.LIGHT){
                    builder = builder.append(x10Device.getState().getBrightness());
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
