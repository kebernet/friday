package com.totsp.home.friday.web.server;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by rcooper on 7/11/15.
 */
public class LogFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger(LogFilter.class.getCanonicalName());
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        LOGGER.info( req.getServletPath()+ req.getPathInfo() +" ContentType: "+req.getHeader("Content-Type")+" Method:"+req.getMethod());
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
