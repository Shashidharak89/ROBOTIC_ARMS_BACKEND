package com.arms.robotic_arms;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RobotTcpLoginServer {

    public void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("✅ TCP Login Server running on port " + port);

            Socket client = serverSocket.accept();
            System.out.println("🤖 Robot connected from " + client.getInetAddress());

            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();

            while (true) {
                byte[] frame = readFrame(in);
                if (frame == null) break;

                System.out.println("📥 RX: " + toHex(frame));

                int msgId = ((frame[1] & 0xFF) << 8) | (frame[2] & 0xFF);

                if (msgId == 0x1002) {
                    System.out.println("➡ LOGIN received");
                    byte[] response = buildLoginAck(frame);
                    System.out.println("📤 TX: " + toHex(response));
                    out.write(response);
                    out.flush();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] buildLoginAck(byte[] req) {
        byte[] hb = new byte[17];
        int i = 0;

        // Header
        hb[i++] = 0x11; // Response ID High
        hb[i++] = 0x01; // Response ID Low
        hb[i++] = 0x00;
        hb[i++] = 0x05;

        // Copy timestamp from request
        System.arraycopy(req, 5, hb, i, 6);
        i += 6;

        // Server Serial
        hb[i++] = 0x00;
        hb[i++] = 0x01;

        // Body
        hb[i++] = req[11]; // original serial high
        hb[i++] = req[12]; // original serial low
        hb[i++] = req[1];  // original msgId high
        hb[i++] = req[2];  // original msgId low
        hb[i++] = 0x00;    // result = success

        byte checksum = 0;
        for (byte b : hb) checksum ^= b;

        byte[] frame = new byte[hb.length + 3];
        frame[0] = 0x7E;
        System.arraycopy(hb, 0, frame, 1, hb.length);
        frame[hb.length + 1] = checksum;
        frame[hb.length + 2] = 0x7E;

        return frame;
    }

    private byte[] readFrame(InputStream in) throws Exception {
        int b;
        do {
            b = in.read();
            if (b == -1) return null;
        } while (b != 0x7E);

        byte[] buf = new byte[2048];
        int idx = 0;
        buf[idx++] = 0x7E;

        while (true) {
            int x = in.read();
            if (x == -1) return null;
            buf[idx++] = (byte) x;
            if (x == 0x7E) break;
        }

        byte[] out = new byte[idx];
        System.arraycopy(buf, 0, out, 0, idx);
        return out;
    }

    private String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) sb.append(String.format("%02X ", b));
        return sb.toString();
    }
}
