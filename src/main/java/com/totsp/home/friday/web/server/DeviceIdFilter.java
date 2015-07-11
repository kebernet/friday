package com.totsp.home.friday.web.server;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Created by rcooper on 7/11/15.
 */
@Singleton
public class DeviceIdFilter implements Filter {
    private static final String houseCodes ="ABCDEFGHIJKLMNOP";
    private static final ThreadLocal<String> DEVICE_ID = new ThreadLocal<>();


    public static String getDeviceId(){
        return DEVICE_ID.get();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        int port = request.getServerPort();
        if(port > 33000) {
            port -= 33000;
            StringBuilder sb = new StringBuilder(Character.toString(houseCodes.charAt((port) / 16)));
            port -= (port / 16) * 16;
            sb.append(port);
            System.out.println(request.getServerPort() + "==" + sb.toString());
            DEVICE_ID.set(sb.toString());
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
