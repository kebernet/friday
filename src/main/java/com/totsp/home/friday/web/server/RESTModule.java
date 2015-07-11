package com.totsp.home.friday.web.server;

import com.google.inject.AbstractModule;
import com.totsp.home.friday.api.DevicesResource;
import com.totsp.home.friday.api.StateResource;
import com.totsp.home.friday.control.Configurator;
import com.totsp.home.friday.control.Dispatch;

import java.util.logging.Logger;

/**
 * Created by rcooper on 7/11/15.
 */
public class RESTModule extends AbstractModule {
    private static final Logger LOGGER = Logger.getLogger(RESTModule.class.getCanonicalName());
    @Override
    public void configure() {
        LOGGER.info("Configuring injector.");
        bind(Configurator.class);
        bind(Dispatch.class).toProvider(DispatchProvider.class);
        //REST endpoints
        bind(DevicesResource.class);
        bind(StateResource.class);

    }
}
