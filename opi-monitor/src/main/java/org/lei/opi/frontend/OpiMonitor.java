package org.lei.opi.frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 *
 * OPI app manager
 *
 * @since 0.0.1
 */
public class OpiMonitor extends Application {

	private double width;
	private double height;

	/**
	 *
	 * Initialize
	 *
	 * @since 0.0.1
	 */
	@Override
	public void init() {
		Rectangle2D bounds = Screen.getPrimary().getBounds();
		width = bounds.getWidth();
		height = bounds.getHeight();
	}

	/**
	 *
	 * Start app
	 *
	 * @param stage Stage, whatever
	 *
	 * @since 0.0.1
	 */
	@Override
	public void start(Stage stage) throws IOException {
		showSplash();
		FXMLLoader fxmlLoader = new FXMLLoader(OpiMonitor.class.getResource("main.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), width / 2, height / 2);
		stage.setTitle("Hello!");
		stage.setScene(scene);
		stage.show();
	}

	// Show splash window
	private void showSplash() {

	}

	public static void main(String[] args) {
		launch();
	}
}