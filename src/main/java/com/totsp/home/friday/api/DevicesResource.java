package com.totsp.home.friday.api;

import com.totsp.home.friday.control.Dispatch;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rcooper on 7/10/15.
 */
@Path("/x10/devices")
@Produces("application/json")
@Consumes({"application/xml","application/json"})
@Singleton
public class DevicesResource {

    private final Dispatch dispatch;

    @Inject
    public DevicesResource(Dispatch dispatch) {
        this.dispatch = dispatch;
    }

    @GET
    public List<Device> list(){
        return new ArrayList<Device>(dispatch.devices());
    }
    @POST
    public List<Device> list2(){
        return new ArrayList<Device>(dispatch.devices());
    }


    @Path("/{id}")
    @GET
    public Device findById(@PathParam("id") String id){
        return dispatch.findDevice(id);
    }

    @Path("/{id}/state")
    @GET
    public State findStateById(@PathParam("id") String id){
        return dispatch.findDevice(id).getState();
    }

    @Path("/{id}/state")
    @POST
    public State updateState(@PathParam("id") String id, State state){
        dispatch.transition(id, state);
        return state;
    }
}
