package com.arms.robotic_arms;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RoboticArmsApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(RoboticArmsApplication.class, args);
	}
	@Override
	public void run(String... args) {
		new Thread(() -> {
			RobotTcpLoginServer server = new RobotTcpLoginServer();
			server.start(8080); // ✅ TCP PORT HERE
		}).start();
	}
}
