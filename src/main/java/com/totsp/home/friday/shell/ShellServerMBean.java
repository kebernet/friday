package com.totsp.home.friday.shell;

import java.io.IOException;

/**
 *
 * @author kebernet
 */
public interface ShellServerMBean {

    void killAllClients();
    void stop();
    void start() throws IOException;
}