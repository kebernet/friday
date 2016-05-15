package com.totsp.home.friday.driver;

import com.totsp.home.friday.api.Device;
import com.totsp.home.friday.x10.Command;
import com.totsp.home.friday.x10.UnitListener;

public interface ControlInterface {


    /** addUnitListener registers the specified UnitListener to recieve
     * ALL events that occur, whether initiated by hardware or software
     * control modules.
     *
     * @param listener the object to recieve UnitEvent objects.
     * @see com.totsp.home.friday.x10.UnitEvent
     */

    void addUnitListener(UnitListener listener);


    /** removeUnitListener unregisters the specified UnitListener.
     * If the specified UnitListener isn't registered, then it is
     * ignored.
     *
     * @param listener the listener to unregister.
     *
     */

    void removeUnitListener(UnitListener listener);


    /** addCommand adds a Command to the queue to be dispatched to
     * all hardware and software com.totsp.home.friday.x10 modules.
     *
     * @param device
     * @param command the Command to be queued.
     *
     */

    void addCommand(Device device, Command command);
}
