package com.totsp.home.friday.x10;


import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CM11A implements Runnable, X10Interface {

    private static final Logger LOGGER = Logger.getLogger(CM11A.class.getCanonicalName());
    private static final byte OK = ((byte) 0x00);
    private static final byte READY = ((byte) 0x55);
    private static final byte TIME = ((byte) 0x9B);
    private static final byte TIME_POLL = ((byte) 0xA5);
    private static final byte DATA_POLL = ((byte) 0x5A);
    private static final byte PC_READY = ((byte) 0xC3);
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private SerialPort sp;
    private volatile boolean running;
    private byte[] lastAddresses;
    private UnitEventDispatcher dispatcher;
    private LinkedBlockingQueue<Command> queue;
    private boolean commandInitiated = false;

    public CM11A(String comport) throws X10Exception {
        try {
            CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(comport);
            sp = (SerialPort) cpi.open("JavaX10Controller", 10000);
            sp.setSerialPortParams(4800, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            //sp.setLowLatency();
            sp.setInputBufferSize(0);
            sp.setOutputBufferSize(0);
            inputStream = new DataInputStream(sp.getInputStream());
            outputStream = new DataOutputStream(sp.getOutputStream());
        } catch (NoSuchPortException|PortInUseException|UnsupportedCommOperationException|IOException e) {
            throw new X10Exception("Unable to connect to serial port: "+e.getMessage(), e);
        }
        queue = new LinkedBlockingQueue<>();
        lastAddresses = new byte[0];
        dispatcher = new UnitEventDispatcher();
        new Thread(this).start();
        this.addUnitListener(new UnitListener() {
            @Override
            public void allUnitsOff(UnitEvent event) {
                LOGGER.info("ALL UNITS OFF");
            }

            @Override
            public void allLightsOff(UnitEvent event) {
                LOGGER.info("ALL LIGHTS OFF");
            }

            @Override
            public void allLightsOn(UnitEvent event) {
                LOGGER.info("ALL LIGHTS ON");
            }

            @Override
            public void unitOn(UnitEvent event) {
                LOGGER.info("UNIT ON " + event.getCommand().getHouseCode() + event.getCommand().getUnitCode());
            }

            @Override
            public void unitOff(UnitEvent event) {
                LOGGER.info("UNIT OFF " + event.getCommand().getHouseCode() + event.getCommand().getUnitCode());
            }

            @Override
            public void unitDim(UnitEvent event) {
                LOGGER.info("UNIT DIM " + event.getCommand().getHouseCode() + event.getCommand().getUnitCode());

            }

            @Override
            public void unitBright(UnitEvent event) {
                LOGGER.info("UNIT BRIGHT " + event.getCommand().getHouseCode() + event.getCommand().getUnitCode());

            }
        });
    }


    /**
     * addUnitListener registers the UnitListener for events.
     *
     * @param listener the listener to register for events.
     */

    public void addUnitListener(UnitListener listener) {
        dispatcher.addUnitListener(listener);
    }


    /**
     * removeUnitListener unregisters the UnitListener for events.
     *
     * @param listener the listener to remove.
     */

    public void removeUnitListener(UnitListener listener) {
        dispatcher.removeUnitListener(listener);
    }


    private byte getChecksum(short packet) {
        byte header = (byte) ((packet >> 8) & 0x00FF);
        byte code = (byte) (packet & 0x00FF);
        return ((byte) ((header + code) & 0xFF));
    }

    private void setInterfaceTime() throws IOException {
        outputStream.writeByte(TIME);
        outputStream.writeByte(0);
        outputStream.writeByte(0);
        outputStream.writeByte(0);
        outputStream.writeByte(0);
        outputStream.writeByte(0);
        outputStream.writeByte(0);
    }


    /**
     * addCommand adds a command to the queue to be dispatched.
     *
     * @param command the Command to be dispatched.
     */

    public void addCommand(Command command) {
        if (queue.peek() != null) {
            queue.add(command);
        } else {
            queue.add(command);
            initiateNextCommand();
        }
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

    /**
     * shutdownNow shuts down the controller and closes the serial port immediately.
     * shutdown(long) is the preferred method of shutting down the controller, but this
     * method provides an immediate, unclean, non-graceful means to shut down the controller.
     */

    public void shutdownNow() {
        SerialPort sp = this.sp;
        this.sp = null;
        sp.close();
        dispatcher.kill();
    }

    private synchronized void initiateNextCommand() {
        System.out.println("intitateNextCommand");
        if (!commandInitiated) {
            Command nextCommand = queue.peek();
            if (nextCommand != null) {
                try {
                    outputStream.writeShort(nextCommand.getAddress());
                    outputStream.flush();
                    System.out.println("Command initiated "+nextCommand.getHouseCode()+nextCommand.getUnitCode());
                    commandInitiated = true;

                } catch (IOException ioe) {
                    if (sp != null) //shutdownNow was not invoked
                    {
                        LOGGER.log(Level.WARNING, "Exception in shutdown", ioe);
                    }
                }
            }
        }
        LOGGER.exiting(CM11A.class.getCanonicalName(), "intitateNextCommand");

    }

    private synchronized void handleChecksum(byte checksum) {
        LOGGER.entering(CM11A.class.getCanonicalName(), "handleChecksum");
        Command nextCommand = queue.peek();
        if (nextCommand != null) {
            System.out.println("Checksum on " + nextCommand.getHouseCode() + nextCommand.getUnitCode());
            if (checksum == getChecksum(nextCommand.getAddress())) {
                try {
                    outputStream.writeByte(OK);
                    outputStream.flush();
                    System.out.println("About to read checksum byte");
                    byte ready = inputStream.readByte();
                    System.out.println("Read checksum byte. "+ready+" READY: "+READY +" running "+running);
                    if(running && ready == READY) {
                        LOGGER.info("Writing command.");
                        outputStream.writeShort(nextCommand.getFunction());
                        outputStream.flush();
                        if(inputStream.readByte() == getChecksum(nextCommand.getFunction())) {
                            outputStream.writeByte(OK);
                            outputStream.flush();
                            ready = inputStream.readByte();
                            if (ready == READY) {
                                dispatcher.dispatchUnitEvent(new UnitEvent(queue.poll()));
                            }
                        }
                    }

                } catch (IOException ioe) {
                    if (sp != null) //shutdownNow was not invoked
                    {
                        LOGGER.log(Level.WARNING, "", ioe);
                    }
                }
            } else {
                LOGGER.fine("CheckSum: " + Integer.toHexString(checksum));
            }
        }

        commandInitiated = false;

    }

    private synchronized void handleData() throws IOException {
        System.out.println("Handle data");
        outputStream.writeByte(PC_READY);
        int length = inputStream.readByte();
        LOGGER.info("Read length "+length);
        if ((length > 0) && (length < 10)) {
            byte detail = inputStream.readByte();
            LOGGER.info("Detail "+detail );
            byte[] data = new byte[length - 1];
            boolean[] isAddr = new boolean[length -1];
            for (int i = 0; i < data.length; i++) {
                LOGGER.info("   about to read:");
                data[i] = inputStream.readByte();
                LOGGER.info("    read byte: "+data[i]);
                isAddr[i] = ((detail % 2) == 0);
                detail = ((byte) (detail >> 1));
            }
            LOGGER.info("read isAddr "+isAddr.length);
            for (int i = 0; i < isAddr.length; i++) {
                LOGGER.info("isAddress "+isAddr[i]);
                if (isAddr[i]) {
                    for (int j = i + 1; j < isAddr.length; j++) {
                        if (!isAddr[j]) {
                            byte function = Command.getFunction(data[j]);
                            byte level = 0;
                            if ((function == Command.DIM) || (function == Command.BRIGHT)) {
                                level = data[j + 1];
                            }
                            lastAddresses = new byte[j - i];
                            for (int k = i; k < j; k++) {
                                lastAddresses[k - i] = data[k];
                                LOGGER.info("Dispatching command " + data[k] + " " + function + " " + level);
                                Command c =new Command(data[k], function, level);
                                LOGGER.info(c.getHouseCode()+""+c.getUnitCode());
                                dispatcher.dispatchUnitEvent(new UnitEvent(new Command(data[k], function, level)));
                            }
                            if ((function == Command.DIM) || (function == Command.BRIGHT)) {
                                i = j + 1;
                            } else {
                                i = j;
                            }
                            j = isAddr.length;
                        }
                    }
                } else {
                    LOGGER.info("Doing non-target op");
                    byte function = data[i];
                    byte level = 0;
                    switch (Command.getFunction(function)) {
                        case Command.ALL_UNITS_OFF:
                        case Command.ALL_LIGHTS_ON:
                        case Command.ALL_LIGHTS_OFF:
                            dispatcher.dispatchUnitEvent(new UnitEvent(new Command(function, function, (byte) 0)));
                            break;
                        case Command.DIM:
                        case Command.BRIGHT:
                            i++;
                            level = data[i];
                        case Command.ON:
                        case Command.OFF:
                            for (int l = 0; l < lastAddresses.length; l++) {
                                dispatcher.dispatchUnitEvent(new UnitEvent(new Command(lastAddresses[l], function, level)));
                            }
                            break;
                    }
                }
            }
        }
    }

    /**
     * run is the thread loop that constantly blocks and reads
     * events off of the serial port from the "CM11A" module.
     */

    public void run() {
        System.out.println("Thread running");
        running = true;
        dispatcher.start();
        try {
            while (running) {
               LOGGER.info("Thread.loop");
                byte nextByte = inputStream.readByte();
                LOGGER.info("Read byte from X10 " + nextByte);
                switch (nextByte) {
                    case TIME_POLL:
                        setInterfaceTime();
                        break;
                    case DATA_POLL:
                        handleData();
                        break;
                    default:
                        handleChecksum(nextByte);
                }
                LOGGER.info("Data available " + inputStream.available());
                if (running && (inputStream.available() == 0) && !queue.isEmpty()) {
                    LOGGER.info("------------------ Calling initiate next command.");
                    initiateNextCommand();
                }
            }
            handleData();
            sp.close();
            notifyAll();
        } catch (IOException ioe) {
            if (sp != null) //shutdownNow was not invoked
            {
                ioe.printStackTrace();
                LOGGER.log(Level.WARNING, "", ioe);
            }
        }
    }
}
