package com.arms.robotic_arms;

import java.net.ServerSocket;
import java.net.Socket;

public class RobotTcpServer {

    public void start() {
        try {
            int tcpPort = 8080;
            ServerSocket serverSocket = new ServerSocket(tcpPort);
            System.out.println("TCP Robot Server running on port " + tcpPort);

            Socket socket = serverSocket.accept();
            System.out.println("Robot connected from " + socket.getInetAddress());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
