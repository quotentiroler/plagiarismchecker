package com.example.plagcheck;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import java.awt.Desktop;
import java.awt.Font;
import java.net.URI;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.example.plagcheck.storage.StorageProperties;
import com.example.plagcheck.storage.StorageService;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
@Slf4j
public class PlagCheckApp {

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false");
		SpringApplication.run(PlagCheckApp.class, args);
	}

	private static void createAndShowGUI() {
		// Create and set up the window
		JFrame frame = new JFrame("PlagCheckApp");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add a label
		JLabel label = new JLabel("Close this window to stop the server.");
		label.setFont(new Font("Arial", Font.PLAIN, 20)); // Set font size to 20
		frame.getContentPane().add(label);

		// Set the window to be square
		frame.setSize(400, 400); // Set size to 400x400 pixels
		frame.setVisible(true);
	}

	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> {
			try {
				if (!Desktop.isDesktopSupported()) {
					log.error("Desktop is not supported");
					return;
				}

				Desktop desktop = Desktop.getDesktop();
				if (!desktop.isSupported(Desktop.Action.BROWSE)) {
					log.error("BROWSE action is not supported");
					return;
				}
				log.info("Opening browser...");
				desktop.browse(new URI("http://localhost:8083"));
			} catch (Exception e) {
				log.error("Failed to open browser", e);
			}
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				SwingUtilities.invokeLater(() -> createAndShowGUI());
			} catch (Exception e) {
				log.error("Failed to open GUI", e);
			}
		};
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			// storageService.deleteAll();
			storageService.init();
		};
	}
}
