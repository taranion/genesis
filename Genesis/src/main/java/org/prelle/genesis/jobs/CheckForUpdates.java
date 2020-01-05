/**
 * 
 */
package org.prelle.genesis.jobs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.logging.log4j.Logger;
import org.prelle.genesis.Constants;
import org.prelle.genesis.Genesis5Main;

import de.rpgframework.ConfigOption;
import de.rpgframework.RPGFrameworkInitCallback;
import de.rpgframework.ResourceI18N;
import de.rpgframework.boot.BootStep;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * @author prelle
 *
 */
public class CheckForUpdates implements BootStep {

	private static Logger logger ;
	private final static ResourceBundle RES = ResourceBundle.getBundle(Genesis5Main.class.getName());

	//-------------------------------------------------------------------
	/**
	 */
	public CheckForUpdates(Logger logger) {
		CheckForUpdates.logger = logger;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.boot.BootStep#getID()
	 */
	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return "UpdateCheck";
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.boot.BootStep#getWeight()
	 */
	@Override
	public int getWeight() {
		// TODO Auto-generated method stub
		return 10;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.boot.BootStep#shallBeDisplayedToUser()
	 */
	@Override
	public boolean shallBeDisplayedToUser() {
		// TODO Auto-generated method stub
		return false;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.boot.BootStep#getConfiguration()
	 */
	@Override
	public List<ConfigOption<?>> getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	private static boolean isNewerVersionAvailable(String installed, String available) { 
		if (available==null)
			return false;

		if (installed.equals(available)) {
			logger.info("Version is up to date");
			return false;
		}

		StringTokenizer tokInst = new StringTokenizer(installed, ". -");
		StringTokenizer tokAvai = new StringTokenizer(available, ". -");

		try {
			for (int i=0; i<3; i++) {
				if (!tokInst.hasMoreTokens() || !tokAvai.hasMoreTokens())
					break;
				Integer valI = Integer.parseInt(tokInst.nextToken());
				Integer valA = Integer.parseInt(tokAvai.nextToken());
				if (valA>valI) {
					logger.info("'"+available+"' is newer than '"+installed+"'");
					return true;
				} else if (valA<valI) {
					logger.info("'"+available+"' is older than '"+installed+"'");
					return false;
				}
			}
		} catch (NumberFormatException e) {
			logger.warn("Failed parsing versions: installed="+installed+"  remote="+available+"  : "+e);
			return false;
		} catch (NoSuchElementException e) {
			logger.warn("Failed parsing versions: installed="+installed+"  remote="+available+"  : "+e);
			return true;
		}

		logger.warn("Assuming that '"+available+"' is newer than '"+installed+"'");
		return true;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.boot.BootStep#execute(de.rpgframework.RPGFrameworkInitCallback)
	 */
	@Override
	public boolean execute(RPGFrameworkInitCallback callback) {
		logger.debug("START: check for updates");
		try {
			if (System.getProperty(Constants.KEY_APPLICATION_VERSION)!=null) {
				String myVersion = System.getProperty(Constants.KEY_APPLICATION_VERSION);
				String branch    = System.getProperty(Constants.KEY_APPLICATION_BRANCH, "genesis-stable");
				URL versionURL = new URL("http://updates.rpgframework.de/"+branch+"/versionNG.txt");
				try {
					HttpURLConnection con = (HttpURLConnection) versionURL.openConnection();
					con.setRequestProperty("User-Agent", "Genesis "+myVersion+" "+System.getProperty("os.name")+"/"+System.getProperty("os.version")+"/"+Locale.getDefault().getDisplayLanguage());
					con.setConnectTimeout(2000);
					con.setReadTimeout(2000);
					if (con.getResponseCode()==200) {
						BufferedReader rin = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
						String firstLine = rin.readLine();
						logger.debug("My local version is: "+myVersion);
						logger.debug("  Remote version is: "+firstLine);
						if (isNewerVersionAvailable(myVersion, firstLine)) {
							logger.info("Inform user that newer version is available");
							String contentText = ResourceI18N.format(RES,"message.new_version", firstLine, myVersion);
							TextArea message = new TextArea(contentText);
							message.setWrapText(true);
							message.setEditable(false);
							message.setStyle("-fx-font-size: 120%;");
							Platform.runLater(() -> {
								Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, contentText, ButtonType.OK);
								alert.getDialogPane().setStyle("-fx-min-width: 50em; -fx-min-height: 20em");
								alert.getDialogPane().setContent(message);
								alert.show();
							});
						} else
							logger.info("Application is up to date");
					} else if (con.getResponseCode()==404) {
						// Cannot find version information
					}
				} catch (Exception e) {
					logger.info("Failed checking for new version");
					String contentText = "There is a problem contacting the webserver at RPGFramework.de: "+e+"\nCheck your internet connection.";
					Label message = new Label(contentText);
					message.setWrapText(true);
					message.setStyle("-fx-font-size: 200%");
					Platform.runLater(() -> {
						Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, contentText, ButtonType.OK);
						alert.show();
					});
				}
			}
			return true;
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		} finally {
			logger.debug("STOP : check for updates");
		}
	}

}
