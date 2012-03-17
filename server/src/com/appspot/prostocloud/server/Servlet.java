package com.appspot.prostocloud.server;

import com.google.appengine.api.memcache.*;
import com.google.appengine.api.utils.*;

import javax.servlet.http.*;
import java.io.*;

public class Servlet extends HttpServlet {

    private static final MemcacheService mc;
    private static final boolean PRODUCTION =
            SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;

    private static class Data implements Serializable {
        String address;
        long timestamp;
    }

    static {
        mc = MemcacheServiceFactory.getMemcacheService();
        // http://groups.google.com/group/google-appengine/msg/cb538fa9b024f362
        Servlet.class.getClassLoader().setDefaultAssertionStatus(true);
        // http://code.google.com/appengine/docs/java/howto/maintenance.html
        mc.setErrorHandler(new StrictErrorHandler());
    }

/*
    curl --data "guid=123456&address=127.0.0.1" http://localhost:8080
    curl --data "guid=123456" http://localhost:8080

    or

    curl --data "guid=123456&address=71.204.138.7" https://prosto-cloud.appspot.com
    curl --data "guid=123456" https://prosto-cloud.appspot.com

*/

    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws javax.servlet.ServletException, java.io.IOException {
        boolean secure = req.isSecure() || !PRODUCTION;
        if (secure && "POST".equals(req.getMethod())) {
            String q = req.getParameter("q");
            String guid = req.getParameter("guid");
            String address = req.getParameter("address");
            if ("a".equals(q)) {
                res.getOutputStream().write(req.getRemoteAddr().getBytes());
                res.setStatus(200);
                return;
            } else if (guid != null && address != null && guid.length() > 0 && address.length() > 0 &&
                address.equals(req.getRemoteAddr())) {
                Data data = new Data();
                data.address = address;
                data.timestamp = System.currentTimeMillis();
                mc.put(guid, data);
                res.setStatus(200);
                return;
            } else if (guid != null && guid.length() > 0) {
                Data data = (Data)mc.get(guid);
                if (data != null) {
                    res.getOutputStream().write(data.address.getBytes());
                    res.setStatus(200);
                    return;
                }
            }

        }
        res.setStatus(400);
    }

}
