package com.arms.robotic_arms;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RobotTcpServer {

    // Static socket reference so REST controllers
    // can send commands to the robot
    private static Socket robotSocket;

    // Output stream to send TCP data to robot
    private static OutputStream robotOut;

    // Starts the TCP server
    public void start(int port) {
        try {
            // Create TCP server socket on given port
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("✅ TCP Server running on port " + port);

            // Wait (block) until robot connects
            robotSocket = serverSocket.accept();
            System.out.println("🤖 Robot connected from " + robotSocket.getInetAddress());

            // Get input stream (robot → server)
            InputStream in = robotSocket.getInputStream();

            // Get output stream (server → robot)
            robotOut = robotSocket.getOutputStream();

            // Infinite loop – TCP is continuous
            while (true) {

                // Read one full packet framed by 0x7E
                byte[] frame = PacketUtil.readFrame(in);

                // If robot disconnects, stop loop
                if (frame == null) break;

                // Print raw packet in hex (for debugging)
                System.out.println("📥 RX: " + PacketUtil.toHex(frame));

                // Extract message ID from packet header
                int msgId = PacketUtil.getMsgId(frame);

                // LOGIN message (0x1002)
                if (msgId == 0x1002) {

                    System.out.println("➡ LOGIN received");

                    // Build ACK (0x1101)
                    byte[] ack = PacketUtil.buildAck(frame);

                    // Send ACK to robot
                    robotOut.write(ack);
                    robotOut.flush();

                    System.out.println("📤 LOGIN ACK sent");
                }

                // STATUS message (0x1003)
                else if (msgId == 0x1003) {

                    System.out.println("➡ STATUS received");

                    // Decode and print status fields
                    PacketUtil.printStatus(frame);

                    // Send ACK for status
                    byte[] ack = PacketUtil.buildAck(frame);
                    robotOut.write(ack);
                    robotOut.flush();
                }
            }

        } catch (Exception e) {
            // Print any error
            e.printStackTrace();
        }
    }

    // This method is called from REST API
    // to send commands (plug / unplug)
    public static void sendCommand(byte[] cmd) throws Exception {

        // Ensure robot is connected
        if (robotOut != null) {

            // Send command packet to robot
            robotOut.write(cmd);
            robotOut.flush();

            System.out.println("📤 COMMAND SENT: " + PacketUtil.toHex(cmd));
        } else {
            System.out.println("⚠ Robot not connected");
        }
    }
}
