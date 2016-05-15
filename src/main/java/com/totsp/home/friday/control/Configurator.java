package com.totsp.home.friday.control;

import com.google.inject.Singleton;
import com.totsp.home.friday.api.Configuration;
import com.totsp.home.friday.api.Controller;
import com.totsp.home.friday.api.Device;
import com.totsp.home.friday.api.X10Device;
import com.totsp.home.friday.driver.ControlInterface;
import com.totsp.home.friday.x10.CM11A;
import com.totsp.home.friday.x10.CM15A;
import com.totsp.home.friday.x10.CM17A;
import com.totsp.home.friday.x10.CM19A;
import com.totsp.home.friday.x10.UnitEventDispatcher;
import com.totsp.home.friday.x10.X10Exception;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by rcooper on 7/10/15.
 */
@Singleton
public class Configurator {

    private static final Logger LOGGER = Logger.getLogger(Configurator.class.getCanonicalName());
    private final String configurationPath;
    private Configuration config;

    public Configurator() {
        this.configurationPath ="/etc/friday/config.xml";
    }

    public Configurator(String configurationPath){
        this.configurationPath = configurationPath;
    }

    public void initialize() throws JAXBException {
        Unmarshaller um = JAXBContext.newInstance(Configuration.class).createUnmarshaller();
        File config = new File(configurationPath);
        if(config.exists()) {
            LOGGER.info("Unmarshalling "+configurationPath);
            this.config = (Configuration) um.unmarshal(config);

            // validate addresses
            HashSet<String> codes = new HashSet<>();
            for(Device device : this.config.getDevices()){
                checkState(!codes.contains(device.getAddress()), device.toString() + " is a duplicate address.");
                codes.add(device.getAddress());
            }
        } else {
            LOGGER.severe("No configuration file at "+configurationPath);
            this.config = new Configuration();
        }
    }

    public void configure(Dispatch dispatch) throws X10Exception {
        UnitEventDispatcher dispatcher = new UnitEventDispatcher();
        HashMap<String, ControlInterface> controllers = new HashMap<>();
        for(Controller controller : config.getControllers()){
            LOGGER.info("Configuring: "+controller);
            ControlInterface instance;
            switch(controller.getType()) {
                case CM11A:
                    instance = new CM11A(dispatcher, controller.getAddress());
                    break;
                case CM17A:
                    instance = new CM17A(dispatcher, controller.getAddress());
                    break;
                case CM15A:
                    String[] tokens = controller.getAddress().split(":");
                    instance = new CM15A(dispatcher, tokens[0], Integer.parseInt(tokens[1]));
                    break;
                case CM19A:
                    tokens = controller.getAddress().split(":");
                    instance = new CM19A(dispatcher, tokens[0], Integer.parseInt(tokens[1]));
                    break;
                default:
                    throw new X10Exception("Unhandled controller type: "+controller.getType());

            }
            controllers.put(controller.getAddress(), instance);
        }
        for(Device device : config.getDevices()){
            if(device instanceof X10Device) {
                X10Device x10Device = (X10Device) device;
                dispatch.addDevice(x10Device, controllers.get(x10Device.getControllerAddress()));
            }
        }
    }

    public Configuration getConfig(){
        return this.config;
    }

}
