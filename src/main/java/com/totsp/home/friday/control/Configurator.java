package com.totsp.home.friday.control;

import com.google.inject.Singleton;
import com.totsp.home.friday.api.Configuration;
import com.totsp.home.friday.api.Controller;
import com.totsp.home.friday.api.Device;
import com.totsp.home.friday.x10.CM11A;
import com.totsp.home.friday.x10.CM17A;
import com.totsp.home.friday.x10.X10Exception;
import com.totsp.home.friday.x10.X10Interface;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

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
        } else {
            LOGGER.severe("No configuration file at "+configurationPath);
            this.config = new Configuration();
        }
    }

    public void configure(Dispatch dispatch) throws X10Exception {
        HashMap<String, X10Interface> controllers = new HashMap<>();
        for(Controller controller : config.getControlers()){
            X10Interface instance;
            switch(controller.getType()) {
                case CM11A:
                    instance = new CM11A(controller.getAddress());
                    break;
                case CM17A:
                    instance = new CM17A(controller.getAddress());
                    break;
                default:
                    throw new X10Exception("Unhandled controller type: "+controller.getType());

            }
            controllers.put(controller.getAddress(), instance);
        }
        for(Device device : config.getDevices()){
            dispatch.addDevice(device, controllers.get(device.getControllerAddress()));
        }
    }

    public Configuration getConfig(){
        return this.config;
    }

}
