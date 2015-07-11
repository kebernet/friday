package com.totsp.home.friday.web.server;

import com.google.inject.Inject;
import com.totsp.home.friday.control.Configurator;
import com.totsp.home.friday.control.Dispatch;
import com.totsp.home.friday.x10.X10Exception;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.bind.JAXBException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by rcooper on 7/10/15.
 */
@Singleton
public class DispatchProvider implements Provider<Dispatch> {
    private static final Logger LOGGER = Logger.getLogger(DispatchProvider.class.getCanonicalName());
    private final Configurator configurator;
    private Dispatch dispatch;

    @Inject
    public DispatchProvider(Configurator configurator) {
        this.configurator = configurator;
    }

    @Override
    public synchronized Dispatch get() {
        if(dispatch == null){
            try {
                configurator.initialize();
            } catch (JAXBException e) {
                throw new RuntimeException("Failed to initialize configuration.", e);
            }
            try {
                configurator.configure(this.dispatch = new Dispatch());
            } catch(X10Exception e){
                LOGGER.log(Level.SEVERE, "Failed to configure X10 devices", e);
            }
        }
        return this.dispatch;
    }
}
