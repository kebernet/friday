package com.totsp.home.friday.web.server;

import com.totsp.home.friday.api.State;
import com.totsp.home.friday.control.Dispatch;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by rcooper on 7/11/15.
 */
@Singleton
public class StateServlet extends HttpServlet {

    private final Dispatch dispatch;

    @Inject
    public StateServlet(Dispatch dispatch) {
        this.dispatch = dispatch;
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        State state = dispatch.findDevice(DeviceIdFilter.getDeviceId()).getState();
        resp.setContentType("application/json");
        if(state != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(resp.getOutputStream(), state);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        State state = mapper.readValue(req.getInputStream(), State.class);
        log("updated "+DeviceIdFilter.getDeviceId());
        dispatch.transition(DeviceIdFilter.getDeviceId(), state);
        resp.setContentType("application/json");
        mapper.writeValue(resp.getOutputStream(), state);
    }
}
