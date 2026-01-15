package com.arms.robotic_arms;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// REST controller for user commands
@RestController
public class RobotCommandController {

    // Called when user clicks "Plug In"
    @GetMapping("/plug")
    public String plug() throws Exception {

        // Build plug-in command
        byte[] cmd = PacketUtil.buildCommand(1, 1);

        // Send command via TCP
        RobotTcpServer.sendCommand(cmd);

        return "Plug command sent";
    }

    // Called when user clicks "Unplug"
    @GetMapping("/unplug")
    public String unplug() throws Exception {

        // Build unplug command
        byte[] cmd = PacketUtil.buildCommand(1, 2);

        // Send command via TCP
        RobotTcpServer.sendCommand(cmd);

        return "Unplug command sent";
    }
}
