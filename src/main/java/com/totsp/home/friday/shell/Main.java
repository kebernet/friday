package com.totsp.home.friday.shell;

import com.totsp.home.friday.x10.CM11A;
import com.totsp.home.friday.x10.X10Interface;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rcooper on 6/29/15.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        ShellServer shell = new ShellServer(6666);
        Map<String, Object> bindings = new HashMap<String, Object>();
        X10Interface controller = new CM11A("/dev/tty.usbserial"); //"/dev/cu.usbserial
        bindings.put("controller", controller);
        shell.setBindings(bindings);
        shell.start();
        ManagementFactory.getPlatformMBeanServer()
                .registerMBean(shell, shell.getJMXObjectName());

    }
}
