package com.totsp.home.friday.control;

import com.totsp.home.friday.api.Configuration;
import com.totsp.home.friday.api.ControllerType;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rcooper on 7/10/15.
 */
public class ConfiguratorTest {

    @Test
    public void testGetConfig() throws Exception {
        Configurator configurator = new Configurator("src/test/resources/testConfig.xml");
        configurator.initialize();
        Configuration config = configurator.getConfig();
        assertEquals("/dev/tty.usbserial", config.getControllers().get(0).getAddress());
        assertEquals(ControllerType.CM11A, config.getControllers().get(0).getType());
        assertEquals("A1", config.getDevices().get(0).getAddress());
    }
}