package com.totsp.home.friday.x10;

import com.google.common.base.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by rcooper on 7/15/15.
 */
public abstract class AbstractMochadInterface implements X10Interface {
    private static final Logger LOGGER = Logger.getLogger(AbstractMochadInterface.class.getCanonicalName());
    protected static final byte XDIM = Byte.MAX_VALUE;
    private volatile boolean running;
    protected LinkedBlockingQueue<Command> queue = new LinkedBlockingQueue<>();
    protected final UnitEventDispatcher dispatcher;
    private final BufferedReader input;
    private final PrintWriter output;
    private final Socket socket;
    private List<Thread> threads = new ArrayList<>(2);


    public AbstractMochadInterface(UnitEventDispatcher dispatcher, String mochadHost, int mochadPort) throws X10Exception {
        this.dispatcher = dispatcher;
        try {
            socket = new Socket(mochadHost, mochadPort);
            this.output = new PrintWriter(socket.getOutputStream());
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException e) {
            throw new X10Exception("Unable to communicate with mochad service", e);
        }
        running = true;
        Thread receiver = new Thread(new Receiver());
        receiver.setDaemon(true);
        threads.add(receiver);

        receiver.start();
        Thread sender = new Thread(new Sender());
        sender.setDaemon(true);
        sender.start();
        threads.add(sender);


    }

    public void shutdown(){
        this.running = false;
        for(Thread t : threads){
            t.interrupt();
        }
    }

    private static String ifUnset(String check, String value){
        return Strings.isNullOrEmpty(check) ? value : check;
    }

    @Override
    public void addUnitListener(UnitListener listener) {
        dispatcher.addUnitListener(listener);
    }

    @Override
    public void removeUnitListener(UnitListener listener) {
        dispatcher.removeUnitListener(listener);
    }

    private static String buildCommandOutput(Command c) throws X10Exception {
        String command = null;
        String address;
        String value = null;
        switch (c.getFunctionByte()) {
            case Command.ALL_LIGHTS_ON:
                command = "all_lights_on";
            case Command.ALL_UNITS_OFF:
                command = ifUnset(command, "all_units_off");
            case Command.ALL_LIGHTS_OFF:
                command = ifUnset(command, "all_lights_off");
                address = Character.toString(c.getHouseCode());
                break;
            case Command.ON:
                command = "on";
            case Command.OFF:
                command = ifUnset(command, "off");
                address = ""+c.getHouseCode() + c.getUnitCode();
                break;
            case Command.BRIGHT:
                command = ifUnset(command, "bright");
            case Command.DIM:
                command = ifUnset(command, "dim");
                value = String.valueOf(Math.round( c.getLevel() ));
                address = ""+c.getHouseCode() + c.getUnitCode();
                break;
            case XDIM:
                command = "xdim";
                value = String.valueOf(Math.round(255f * ((float) c.getLevel() / 100)));
                address = ""+c.getHouseCode() + c.getUnitCode();
                break;
            default:
                throw new X10Exception("Unknown command byte " + c.getFunctionByte());
        }
        StringBuilder sb = new StringBuilder( c.isUseRadioFrequency() ? "rf" : "pl")
                .append(" ")
                .append(address.toLowerCase())
                .append(" ")
                .append(command);
        if(value != null){
            sb = sb.append(" ").append(value);
        }
        return sb.toString();
    }

    class Receiver implements Runnable {

        @Override
        public void run() {
            while(running){
                try {
                    String line = input.readLine();
                    //LOGGER.info("Read: "+line);
                    String[] tokens = line.split(" ");
                    if("rx".equalsIgnoreCase(tokens[2])){
                        byte function = Command.ON;
                        switch(tokens[7].toLowerCase()) {
                            case "off":
                                function = Command.OFF;
                                break;
                            case "on":
                                function = Command.ON;
                                break;
                        }
                        Command c = new Command(tokens[5],function);
                        dispatcher.dispatchUnitEvent(new UnitEvent(c));
                    } else if("tx".equalsIgnoreCase(tokens[2])){
                        //LOGGER.info("Advancing.");

                    }
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Exception reading from Mochad input: ",e);
                }
            }
        }
    }

    class Sender implements Runnable {

        @Override
        public void run() {
            while(running){
                Command c = null;
                try {
                    c = queue.poll(10, TimeUnit.MINUTES);

                    if(c == null){
                        continue;
                    }

                    System.out.println(queue.size() + "Sending "+buildCommandOutput(c));
                    output.println(buildCommandOutput(c));
                    output.flush();
                    if(c.isUseRadioFrequency()) {
                        Thread.sleep(1600);
                    }
                } catch(X10Exception e){
                    LOGGER.log(Level.WARNING, "Failed to write command "+c, e);
                } catch(InterruptedException e){
                    LOGGER.log(Level.WARNING, "Interrupted", e);
                }
            }
        }
    }
}
