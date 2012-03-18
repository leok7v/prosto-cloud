package com.appspot.prostocloud.client;

import java.io.*;
import java.net.*;
import java.util.*;

import static com.appspot.prostocloud.client.ProstoCloud.trace;
import static com.appspot.prostocloud.client.io.*;
import static com.appspot.prostocloud.client.str.*;
import static com.appspot.prostocloud.client.util.encodeURL;


@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class http {

    public static String get(String endpoint) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection)new URL(endpoint).openConnection();
            c.setRequestMethod("GET");
            c.setUseCaches(false);
            int code = c.getResponseCode();
            if (code == 200) {
                byte[] raw = readFullyAndClose(c.getInputStream());
                return trim(new String(raw));
            } else {
                trace("http.get(" + endpoint +") returned " + code);
                Thread.dumpStack();
                return "";
            }
        } catch (Throwable e) {
            // MalformedURLException, ProtocolException, IOException
            trace("http.get(" + endpoint +") failed " + e.getMessage());
            e.printStackTrace();
            return "";
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
    }

    public static String post(String endpoint, String data) {
        java.net.HttpURLConnection c = null;
        try {
            c = (java.net.HttpURLConnection)new URL(endpoint).openConnection();
            c.setRequestMethod("POST");
            c.setUseCaches(false);
            c.setDoInput(true);
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            c.setRequestProperty("Content-Length", "" + data.length());
            OutputStreamWriter wr = new OutputStreamWriter(c.getOutputStream());
            wr.write(data);
            wr.flush();
            wr.close();
            int code = c.getResponseCode();
            if (code == 200) {
                byte[] raw = readFullyAndClose(c.getInputStream());
                return trim(new String(raw));
            } else {
                trace("http.post(" + endpoint + ", " + data +") returned " + code);
                Thread.dumpStack();
                return "";
            }
        } catch (Throwable e) {
            // MalformedURLException, ProtocolException, IOException
            trace("http.post(" + endpoint + ", " + data +") failed " + e.getMessage());
            e.printStackTrace();
            return "";
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
    }

    public static String post(String endpoint, Map<String, String> fd) {
        StringBuilder sb = new StringBuilder(fd.size() * 100);
        for (Map.Entry<String, String> e : fd.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(encode(e.getKey(), e.getValue()));
        }
        return post(endpoint, sb.toString());
    }

    private static String encode(String n, String v) {
        return encodeURL(n) + "=" + encodeURL(v);
    }

}
