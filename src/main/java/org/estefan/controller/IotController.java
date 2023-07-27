package org.estefan.controller;

import com.sun.net.httpserver.HttpServer;
import org.estefan.handler.DeviceRequestHandler;
import org.estefan.handler.UserRequestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class IotController {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("ec2-18-220-79-28.us-east-2.compute.amazonaws.com",8080), 0);
        server.createContext("/userTempProg", new UserRequestHandler());
        server.createContext("/deviceTempProg", new DeviceRequestHandler());
        server.setExecutor(null);
        server.start();
    }
}
