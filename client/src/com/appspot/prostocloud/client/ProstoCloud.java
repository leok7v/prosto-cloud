package com.appspot.prostocloud.client;

import android.app.Activity;
import android.content.*;
import android.net.*;
import android.os.Bundle;
import android.util.*;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class ProstoCloud extends Activity {

    private static TextView tv;

    public static void trace(String s) {
        Log.d("prosto", s);
        tv.setText(tv.getText() + "\n" + s);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup v = (ViewGroup)inflater.inflate(R.layout.main, null);
        tv = (TextView)v.getChildAt(0);
        setContentView(v);
        Set<InetAddress> a = getLocalIpAddress();

        Context context = getApplicationContext();
        CharSequence text = a.toString() + isConnected();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        trace(getLocalIpAddress().toString() + isConnected());
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("q", "a");
        String ip = http.post("https://prosto-cloud.appspot.com", data);
        trace(ip);
        try {
            InetAddress i = InetAddress.getByName(ip);
            Server.start(i);
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
        for (InetAddress i : a) {
            Server.start(i);
        }
    }

    public Set<InetAddress> getLocalIpAddress() {
        Set<InetAddress> a = new HashSet<InetAddress>();
        try {
            Enumeration<NetworkInterface> ne = NetworkInterface.getNetworkInterfaces();
            while (ne.hasMoreElements()) {
                NetworkInterface ni = ne.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ia = addresses.nextElement();
                    if (!ia.isLoopbackAddress()) {
                        a.add(ia);
                    }
                }
            }
            return a;
        } catch (SocketException ex) {
            throw new Error(ex);
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.getState() == NetworkInfo.State.CONNECTED;
    }


    private static class Server extends Thread {

        private InetAddress address;

        public static void start(InetAddress a) {
            Server s = new Server();
            s.address = a;
            s.setDaemon(true);
            s.start();
        }

        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(8888, 0, address);
                System.out.println("Listening :8888");
                for (;;) {
                    Socket socket = null;
                    DataInputStream in = null;
                    DataOutputStream out = null;
                    try {
                        socket = serverSocket.accept();
                        in = new DataInputStream(socket.getInputStream());
                        out = new DataOutputStream(socket.getOutputStream());
                        trace("ip: " + socket.getInetAddress());
                        trace("message: " + in.readUTF());
                        out.writeUTF("Hello!");
                    } finally {
                        if (socket != null) {
                            socket.close();
                        }
                        io.close(in);
                        io.close(out);
                    }
                }
            } catch (IOException e) {
                trace("Server failed for address=" + address + " " + e.getMessage());
            }
        }
    }




}
