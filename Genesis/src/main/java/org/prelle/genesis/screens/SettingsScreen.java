/**
 * 
 */
package org.prelle.genesis.screens;

import java.util.ResourceBundle;

import org.prelle.genesis.ExternalTools;
import org.prelle.genesis.Genesis5Main;
import org.prelle.genesis.page.ExternalToolsPage;
import org.prelle.genesis.page.GenesisConfigurationPage;
import org.prelle.genesis.page.LicensePage;
import org.prelle.genesis.page.PluginsPage;
import org.prelle.javafx.ManagedScreen;

import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;

/**
 * @author prelle
 *
 */
public class SettingsScreen extends ManagedScreen {

	private final static ResourceBundle GENESIS = ResourceBundle.getBundle(SettingsScreen.class.getName());

	private ExternalTools external;
	
	private GenesisConfigurationPage pgGeneral;
	private LicensePage pgLicense;
	private ExternalToolsPage pgExternal;
//	private OnlineServicesPage pgOnline;
	private PluginsPage pgPlugins;
//	private AboutPage pgAbout;
	
	private MenuItem btnOnline;
	private MenuItem btnGeneral;
	private MenuItem btnPlugins;
	private MenuItem btnLicenses;
	private MenuItem btnOpenWith;
	private MenuItem btnAbout;
	
	//-------------------------------------------------------------------
	public SettingsScreen(ExternalTools external) {
		this.external = external;
		
		initComponents();
		initLayout();
		initInteractivity();
	}
	
	//-------------------------------------------------------------------
	private void initComponents() {
		btnGeneral  = new MenuItem(GENESIS.getString("settings.general"), new Label("\uE713"));
		btnOnline   = new MenuItem(GENESIS.getString("settings.online"), new Label("\uE909")); // E128
		btnPlugins  = new MenuItem(GENESIS.getString("settings.plugins"), new Label("\uE718"));
		btnLicenses = new MenuItem(GENESIS.getString("settings.licenses"), new Label("\uE8D7")); // E192
		btnOpenWith = new MenuItem(GENESIS.getString("settings.openwidth"), new Label("\uE90F")); // E0ED
		btnAbout    = new MenuItem(GENESIS.getString("settings.about"), new Label("\uE8FD")); // E17D
		
		getNavigationItems().addAll(btnGeneral, btnPlugins, btnLicenses, btnAbout);
		
		pgGeneral = new GenesisConfigurationPage();
		pgLicense = new LicensePage();
//		pgExternal= new ExternalToolsPage(external);
//		pgOnline  = new OnlineServicesPage();
		pgPlugins = new PluginsPage();
//		pgAbout   = new AboutPage();
	}
	
	//-------------------------------------------------------------------
	private void initLayout() {
		setLandingPage(pgGeneral);
	}
	
	//-------------------------------------------------------------------
	private void initInteractivity() {
		btnGeneral .setOnAction(event -> showPage(pgGeneral));
		btnLicenses.setOnAction(event -> showPage(pgLicense));
//		btnOnline  .setOnAction(event -> showPage(pgOnline));
		btnPlugins .setOnAction(event -> showPage(pgPlugins));
		btnOpenWith.setOnAction(event -> showPage(pgExternal));
//		btnAbout   .setOnAction(event -> showPage(pgAbout));
	}

	//-------------------------------------------------------------------
	public void refresh() {
//		logger.info("refresh()-----------------------------------------------");
		pgPlugins.refresh();
	}
}
