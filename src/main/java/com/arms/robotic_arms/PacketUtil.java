package com.arms.robotic_arms;

import java.io.InputStream;

public class PacketUtil {

    // -------------------------------
    // READ A FULL TCP FRAME
    // -------------------------------
    public static byte[] readFrame(InputStream in) throws Exception {

        int b;

        // Keep reading until we find start byte 0x7E
        do {
            b = in.read();
            if (b == -1) return null; // connection closed
        } while (b != 0x7E);

        // Temporary buffer
        byte[] buffer = new byte[2048];
        int index = 0;

        // Store start byte
        buffer[index++] = 0x7E;

        // Read until end byte 0x7E
        while (true) {
            int x = in.read();
            if (x == -1) return null;

            buffer[index++] = (byte) x;

            if (x == 0x7E) break; // end of frame
        }

        // Copy exact frame size
        byte[] frame = new byte[index];
        System.arraycopy(buffer, 0, frame, 0, index);

        return frame;
    }

    // -------------------------------
    // EXTRACT MESSAGE ID
    // -------------------------------
    public static int getMsgId(byte[] frame) {
        // Message ID is bytes 1 and 2
        return ((frame[1] & 0xFF) << 8) | (frame[2] & 0xFF);
    }

    // -------------------------------
    // BUILD ACK (0x1101)
    // Used for LOGIN and STATUS
    // -------------------------------
    public static byte[] buildAck(byte[] req) {

        // Header + body size = 17 bytes
        byte[] hb = new byte[17];
        int i = 0;

        // Response message ID = 0x1101
        hb[i++] = 0x11;
        hb[i++] = 0x01;

        // Body length = 5 bytes
        hb[i++] = 0x00;
        hb[i++] = 0x05;

        // Copy timestamp from request (important!)
        System.arraycopy(req, 5, hb, i, 6);
        i += 6;

        // Server serial number
        hb[i++] = 0x00;
        hb[i++] = 0x01;

        // Body fields
        hb[i++] = req[11]; // original robot serial high
        hb[i++] = req[12]; // original robot serial low
        hb[i++] = req[1];  // original msgId high
        hb[i++] = req[2];  // original msgId low
        hb[i++] = 0x00;    // result = SUCCESS

        // XOR checksum
        byte checksum = 0;
        for (byte b : hb) checksum ^= b;

        // Final frame = 0x7E + hb + checksum + 0x7E
        byte[] frame = new byte[20];
        frame[0] = 0x7E;
        System.arraycopy(hb, 0, frame, 1, hb.length);
        frame[18] = checksum;
        frame[19] = 0x7E;

        return frame;
    }

    // -------------------------------
    // PARSE STATUS PACKET
    // -------------------------------
    public static void printStatus(byte[] frame) {

        // Body starts after header (index 13)
        int bodyStart = 13;

        // Parking number (2 bytes)
        int parking =
                ((frame[bodyStart] & 0xFF) << 8)
                        | (frame[bodyStart + 1] & 0xFF);

        // Working status
        int workStatus = frame[bodyStart + 5] & 0xFF;

        // Fault code
        int fault = frame[bodyStart + 8] & 0xFF;

        System.out.println(
                "🧾 STATUS → Parking=" + parking +
                        ", WorkStatus=" + workStatus +
                        ", Fault=" + fault
        );
    }

    // -------------------------------
    // BUILD COMMAND (0x1103)
    // -------------------------------
    public static byte[] buildCommand(int parking, int actionType) {

        // actionType:
        // 1 = plug in
        // 2 = unplug

        byte[] hb = new byte[25];
        int i = 0;

        // Message ID = 0x1103
        hb[i++] = 0x11;
        hb[i++] = 0x03;

        // Body length = 13 bytes
        hb[i++] = 0x00;
        hb[i++] = 0x0D;

        // Dummy timestamp
        hb[i++] = 0x19;
        hb[i++] = 0x09;
        hb[i++] = 0x19;
        hb[i++] = 0x10;
        hb[i++] = 0x10;
        hb[i++] = 0x10;

        // Server serial
        hb[i++] = 0x00;
        hb[i++] = 0x02;

        // Body
        hb[i++] = (byte) (parking >> 8);
        hb[i++] = (byte) parking;
        hb[i++] = 0x00;
        hb[i++] = 0x00;
        hb[i++] = (byte) actionType;
        hb[i++] = 0x00;
        hb[i++] = 0x00;
        hb[i++] = 0x00;
        hb[i++] = 0x00;
        hb[i++] = 0x00;
        hb[i++] = 0x00;
        hb[i++] = 0x00;
        hb[i++] = 0x01;

        // XOR checksum
        byte checksum = 0;
        for (byte b : hb) checksum ^= b;

        // Final frame
        byte[] frame = new byte[28];
        frame[0] = 0x7E;
        System.arraycopy(hb, 0, frame, 1, hb.length);
        frame[26] = checksum;
        frame[27] = 0x7E;

        return frame;
    }

    // -------------------------------
    // HEX STRING FOR DEBUG
    // -------------------------------
    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}
