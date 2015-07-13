package com.totsp.home.friday.web.server;

import com.google.inject.servlet.ServletModule;
import com.totsp.home.friday.control.Configurator;
import com.totsp.home.friday.control.Dispatch;

import java.util.logging.Logger;

/**
 * Created by rcooper on 7/11/15.
 */
public class RESTModule extends ServletModule {
    private static final Logger LOGGER = Logger.getLogger(RESTModule.class.getCanonicalName());

    @Override
    protected void configureServlets() {
        LOGGER.info("Configuring injector.");
        bind(Configurator.class);
        bind(Dispatch.class).toProvider(DispatchProvider.class);
        filter("/*").through(LogFilter.class);
        filter("/*").through(DeviceIdFilter.class);
        serve("/state").with(StateServlet.class);
    }
}
