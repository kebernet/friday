package com.totsp.home.friday.web.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by rcooper on 7/10/15.
 */
public class Application extends ResourceConfig {

    private static final Logger LOGGER = Logger.getLogger(Application.class.getCanonicalName());

    @Inject
    public Application(ServiceLocator serviceLocator) {
        // Set package to look for resources in
        packages("com.totsp.home.friday.api");

        LOGGER.info("Registering with Jersey...");

        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
        GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        Injector injector = Guice.createInjector(new RESTModule());
        guiceBridge.bridgeGuiceInjector(injector);
        LOGGER.info("Registered injector " + injector);
    }
}