package org.prelle.genesis;

import java.util.List;

import de.rpgframework.ConfigOption;
import de.rpgframework.RPGFrameworkInitCallback;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

class GenesisSplashScreen extends StackPane implements RPGFrameworkInitCallback {

	private ProgressBar bar;
	private final Label status;
	private Label version;

	//-------------------------------------------------------------------
	public GenesisSplashScreen() {
//		System.out.println("GenesisSplashScreen.<init>: 1: "+ClassLoader.getSystemResourceAsStream("org/prelle/genesis/images/splash.png"));
//		System.out.println("GenesisSplashScreen.<init>: 2: "+ClassLoader.getSystemResourceAsStream("/org/prelle/genesis/images/splash.png"));
//		System.out.println("GenesisSplashScreen.<init>: 3: "+ClassLoader.getSystemResourceAsStream("images/splash.png"));
//		System.out.println("GenesisSplashScreen.<init>: 4: "+this.getClass().getResourceAsStream("images/splash.png"));
		Image logo = new Image(this.getClass().getResourceAsStream("images/splash.png"));
		ImageView iView = new ImageView(logo);

		bar = new ProgressBar();
		String versText = System.getProperty(Constants.KEY_APPLICATION_VERSION, "Run in IDE");
		String profText = System.getProperty(Constants.KEY_PROFILE, "NoProfile");
		version = new Label("Version: "+versText+"\nProfile: "+profText);
		version.setWrapText(true);
		version.setTextAlignment(TextAlignment.CENTER);
		status = new Label("Checking for updates");
		status.setWrapText(true);
		status.setStyle(
				"-fx-effect: dropshadow(gaussian, white, 5, 0.7, 0, 0);" +
						"-fx-text-fil: black;" +
						"-fx-font-size: large"
				);
		version.setStyle(
				"-fx-effect: dropshadow(gaussian, white, 5, 0.7, 0, 0);" +
						"-fx-text-fil: black;" +
						"-fx-font-size: large"
				);

		StackPane innerStack = new StackPane();
		innerStack.setStyle("-fx-background-color: transparent; -fx-background-insets: 00;");
		innerStack.getChildren().addAll(status, bar);

		VBox vbox = new VBox(20);
		vbox.setAlignment(Pos.TOP_CENTER);
		vbox.getChildren().addAll(innerStack, version);

		setStyle("-fx-background-color: transparent;-fx-background-insets: 00;");
		getChildren().addAll(iView, vbox);
		StackPane.setMargin(status, new Insets(160,0,0,0));
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.RPGFrameworkInitCallback#progressChanged(double)
	 */
	@Override
	public void progressChanged(double value) {
		Platform.runLater( () -> {
			bar.setProgress(value);
		});
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.RPGFrameworkInitCallback#message(java.lang.String)
	 */
	@Override
	public void message(String mess) {
		Platform.runLater( () -> {
			status.setText(mess);
			status.setStyle(
					"-fx-effect: dropshadow(gaussian, white, 5, 0.7, 0, 0);" +
							"-fx-text-fil: black;" +
							"-fx-font-size: large"
					);
		});
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.RPGFrameworkInitCallback#errorOccurred(java.lang.String, java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void errorOccurred(String location, String detail, Throwable exception) {
		Platform.runLater( () -> {
			status.setText(detail);
			status.setStyle(
					"-fx-effect: dropshadow(gaussian, white, 5, 0.7, 0, 0);" +
							"-fx-text-fil: red;" +
							"-fx-font-size: large"
					);
		});
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.RPGFrameworkInitCallback#showConfigOptions(java.lang.String, java.util.List)
	 */
	@Override
	public void showConfigOptions(String id, List<ConfigOption<?>> configuration) {
		// TODO Auto-generated method stub

	}

}