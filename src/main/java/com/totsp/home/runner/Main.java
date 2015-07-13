package com.totsp.home.runner;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Created by rcooper on 7/11/15.
 */
public class Main {

    public static void main(String... args) throws Exception {
        QueuedThreadPool pool = new QueuedThreadPool(1286);
        Server server = new Server(pool);
        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory());
        connector.setPort(33000);
        server.addConnector(connector);
        for(int i=1; i < 257; i++){
            connector = new ServerConnector(server, new HttpConnectionFactory());
            connector.setPort(33000 + i);
            server.addConnector(connector);
        }
        server.setStopAtShutdown(true);
        WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setContextPath("/");

        context.setWar("web");
        server.setHandler(context);

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
    }
}
