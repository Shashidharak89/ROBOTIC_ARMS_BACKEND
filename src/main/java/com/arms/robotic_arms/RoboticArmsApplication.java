package com.arms.robotic_arms;

// CommandLineRunner allows us to run custom code
// AFTER Spring Boot has started
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Marks this as a Spring Boot application
@SpringBootApplication
public class RoboticArmsApplication implements CommandLineRunner {

	// Main method – JVM starts execution from here
	public static void main(String[] args) {
		// Starts Spring Boot (web server, beans, etc.)
		SpringApplication.run(RoboticArmsApplication.class, args);
	}

	// This method runs automatically AFTER Spring Boot starts
	@Override
	public void run(String... args) {

		// We start the TCP server in a new thread
		// because TCP server is a BLOCKING process
		// and must not block Spring Boot
		new Thread(() -> {

			// Create TCP server instance
			RobotTcpServer server = new RobotTcpServer();

			// Start TCP server on port 8080
			// Robot will connect to this port
			server.start(8080);

		}).start(); // Start the thread
	}
}
