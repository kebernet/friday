package com.totsp.home.friday.x10;

import com.totsp.home.friday.api.Device;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import java.io.*;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


public class CM17A implements Runnable, X10Interface {

    private static final Logger LOGGER = Logger.getLogger(CM17A.class.getCanonicalName());
    private final long WAIT_INTERVAL = 1L;
    private final long RESET_INTERVAL = 10L;
    private final long COMMAND_INTERVAL = 1000L;

    private final String HEADER = "1101010110101010";
    private final String FOOTER = "10101101";

    private final String FIRECRACKER_MAP_FILE = "com/totsp/home/friday/x10/cm17aCommand.map";

    private SerialPort sp;
    private boolean running;
    private final UnitEventDispatcher dispatcher;
    private final ConcurrentLinkedQueue<Command> queue;
    private static final Command STOP = new Command("A1", Command.DIM, 0);
    private final Hashtable commandTable;


    /**
     * CM17A constructs and starts the X10Interface on the
     * specified comport.  On a Windows based PC, the comport is of the
     * form "COM1".
     *
     * @param comport the communications port in which the "Firecracker"
     *                module is connected.
     * @throws IOException if an error occurs while trying to connect
     *                     to the specified Communications Port.
     */

    public CM17A(UnitEventDispatcher dispatcher, String comport) throws X10Exception {
        try {
            CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(comport);
            sp = (SerialPort) cpi.open("JavaX10Controller", 10000);
        } catch (NoSuchPortException|PortInUseException e) {
           throw new X10Exception("Failed to open comport", e);
        }
        commandTable = new Hashtable();
        ClassLoader loader = getClass().getClassLoader();
        InputStream commandStream = loader.getResourceAsStream(FIRECRACKER_MAP_FILE);
        if (commandStream == null) {
            throw new X10Exception("Failed locate map", new FileNotFoundException(FIRECRACKER_MAP_FILE));
        } else {
            try {
                BufferedReader commandReader = new BufferedReader(new InputStreamReader(commandStream));
                String nextLine = commandReader.readLine();
                while (nextLine != null) {
                    String unitCode = nextLine.substring(0, 4).trim();
                    String command = nextLine.substring(4, 16).trim();
                    String code = nextLine.substring(16).trim();

                    commandTable.put(unitCode + command, code);
                    nextLine = commandReader.readLine();
                }
                commandReader.close();
            } catch(IOException ioe){
                throw new X10Exception("Failed to read map", ioe);
            }
        }

        queue = new ConcurrentLinkedQueue<>();
        this.dispatcher = dispatcher;
        new Thread(this).start();
    }


    public void addUnitListener(UnitListener listener) {
        dispatcher.addUnitListener(listener);
    }


    public void removeUnitListener(UnitListener listener) {
        dispatcher.removeUnitListener(listener);
    }


    public void addCommand(Device device, Command command) {
        queue.add(command);
    }


    /**
     * finalize disconnects the serial port connection and closes
     * the X10Interface.
     */

    protected void finalize() {
        addCommand(null, STOP);
        dispatcher.kill();
    }

    private synchronized void doNotify() {
        notifyAll();
    }

    private synchronized void doWait(long millis) throws InterruptedException {
        wait(millis);
    }

    /**
     * shutdown tells the controller to finish all commands
     * in the queue and then gracefully disconnects the serial
     * port connection.
     *
     * @param millis the number of milliseconds to wait for a graceful shutdown.
     * @throws OperationTimedOutException thrown if the X10Interface has not
     *                                    completely shutdown in the amount of time specified.
     * @throws InterruptedException       thrown if the thread is unexpectedly interrupted
     */

    public void shutdown(long millis) throws OperationTimedOutException, InterruptedException {
        if (running) {
            try {
                finalize();
                doWait(millis);
                if (running) {
                    throw new OperationTimedOutException("Timed out while waiting for CM11A to shutdown");
                }
            } catch (InterruptedException ie) {
                if (running) {
                    throw ie;
                }
            }
        }
    }

   public void shutdownNow() {
        sp.close();
        finalize();
    }

    public void run() {
        running = true;
        dispatcher.start();
        resetFirecracker();
        while (running) {
            try {
                Command nextCommand = (Command) queue.poll();
                if (nextCommand == STOP) {
                    running = false;
                } else {
                    char houseCode = nextCommand.getHouseCode();
                    int unitCode = nextCommand.getUnitCode();
                    short function = nextCommand.getFunctionByte();
                    switch (function) {
                        case Command.ON:
                            toFirecracker((String) commandTable.get(houseCode + "" + unitCode + "ON"));
                            break;
                        case Command.OFF:
                            toFirecracker((String) commandTable.get(houseCode + "" + unitCode + "OFF"));
                            break;
                        case Command.DIM:
                        case Command.BRIGHT:
                        case Command.ALL_LIGHTS_OFF:
                        case Command.ALL_LIGHTS_ON:
                        case Command.ALL_UNITS_OFF:
                    }
                    dispatcher.dispatchUnitEvent(new UnitEvent(nextCommand));
                    sleep(COMMAND_INTERVAL);
                }
            } catch (RuntimeException ie) {
            }
        }
        sp.close();
        doNotify();
    }

    private void resetFirecracker() {
        sp.setDTR(false);
        sp.setRTS(false);
        sleep(RESET_INTERVAL);

        sp.setDTR(true);
        sp.setRTS(true);
        sleep(RESET_INTERVAL);
    }

    private void toFirecracker(String transmission) {
        transmission = HEADER + transmission + FOOTER;
        int len = transmission.length();

        for (int i = 0; i < len; i++) {
            if (transmission.charAt(i) == '0') {
                sp.setRTS(false);
                sleep(WAIT_INTERVAL);
                sp.setRTS(true);
                sleep(WAIT_INTERVAL);
            } else {
                sp.setDTR(false);
                sleep(WAIT_INTERVAL);
                sp.setDTR(true);
                sleep(WAIT_INTERVAL);
            }
        }
    }

    private void sleep(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException ie) {
        }
    }
}
