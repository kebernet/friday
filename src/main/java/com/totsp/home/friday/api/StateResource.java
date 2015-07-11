package com.totsp.home.friday.api;

import com.totsp.home.friday.control.Dispatch;
import com.totsp.home.friday.web.server.DeviceIdFilter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * Created by rcooper on 7/11/15.
 */
@Path("/x10/state")
@Produces("application/json")
@Consumes({"application/xml","application/json"})
@Singleton
public class StateResource {

    private final Dispatch dispatch;

    @Inject
    public StateResource(Dispatch dispatch) {
        this.dispatch = dispatch;
    }

    @GET
    public State findStateById(){
        return dispatch.findDevice(DeviceIdFilter.getDeviceId()).getState();
    }

    @POST
    public State updateState(State state){
        dispatch.transition(DeviceIdFilter.getDeviceId(), state);
        return state;
    }
}
