package com.asisoft.caen.host.main;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.xml.ws.Endpoint;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.asisoft.caen.host.util.PipelineHelper;

import com.asisoft.caen.host.connector.CaenController;
import com.asisoft.caen.host.service.CaenRFIDProxy;

/**
 * Entry point for the whole application
 * 
 * @author "David Figueroa"
 * 
 */
public class Main {

	/**
	 * Singleton instance
	 */
	private static Main myself;

	private static Logger log = Logger.getLogger(Main.class);

	/**
	 * Web service instance
	 */
	private CaenRFIDProxy proxyService;

	/**
	 * Host where the web service is attached
	 */
	private String host;

	/**
	 * Web service listening port
	 */
	private int port;

	/**
	 * Serial port where the reader is attached
	 */
	private String serialPort;

	/**
	 * CAEN reader logical source to query
	 */
	private String logicalSource;

	/**
	 * CAEN reader power to be set
	 */
	private int readerPower;

	/**
	 * Configuration file path
	 */
	private String defaultConfigFile = "/props/CaenProxyService_default.xml";

	/**
	 * The configuration
	 */
	private XMLConfiguration config = null;

	/**
	 * private constructor
	 */
	private Main() {
	}

	/**
	 * Logger configuration
	 */
	private void configureLogger() {
		// Loading log4j.properties configuration

		// Configuring using the internal jar resource
		// PropertyConfigurator.configure(getClass().getResource("/props/log4j.properties"));

		// Overwriting internal configuration with any external properties file
		String fileurl = PipelineHelper.getResourcePath("/props/log4j.properties");
		
		PropertyConfigurator.configure(fileurl);
	}

	/**
	 * Loads configuration attributes
	 */
	public void loadConfiguration() {

		this.config = new XMLConfiguration();
		config.setListDelimiter(',');
		
		String fileurl = PipelineHelper.getResourcePath(defaultConfigFile);

		log.info("Config path: " + fileurl);

		try {
			config.load(fileurl);

			this.host = config.getString("host");
			this.port = config.getInt("port", 8888);
			this.serialPort = config.getString("serialPort");
			this.logicalSource = config.getString("logicalSource");
			this.readerPower = config.getInt("readerPower", 199);

			log.info("Service host configuration value: " + this.host);
			log.info("Service host port: " + this.port);
			log.info("Reader serial port: " + this.serialPort);
			log.info("Reader logical source: " + this.logicalSource);
			log.info("Reader power configuration value: " + this.readerPower);

		} catch (ConfigurationException e) {
			log.error("Configuration file could not be opened: " + e.getMessage());
		}
	}

	/**
	 * Starts the local Web service
	 */
	public void createService() {

		CaenController caenController = new CaenController(this.serialPort, this.logicalSource, this.readerPower);

		// Creating the web service wrapper of the Caen reader controller
		proxyService = new CaenRFIDProxy(caenController);

		String endpoint = "http://" + this.host + ":" + this.port + "/caenrfid-proxy";

		Endpoint.publish(endpoint, proxyService);
	}

	/**
	 * 
	 */
	public void configureSystemTray() {

		if (!SystemTray.isSupported()) {
			return;
		}

		SystemTray tray = SystemTray.getSystemTray();

		Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/icon.png"));

		final TrayIcon trayIcon = new TrayIcon(image, "Caen RFID Proxy");
		trayIcon.setImageAutoSize(true);

		trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "Desarrollado por Ariadna Servicios Informáticos";
				trayIcon.displayMessage("Acerca de", message, TrayIcon.MessageType.INFO);
			}
		});

		PopupMenu popUp = new PopupMenu();

		MenuItem exitItem = new MenuItem("Salir");
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				log.info("Finishing the application...");
				System.exit(0);
			}
		});

		popUp.add(exitItem);

		trayIcon.setPopupMenu(popUp);

		try {
			tray.add(trayIcon);

		} catch (AWTException e) {

			log.warn("Tray icon couldn't be loaded" + e.getMessage());
		}
	}

	/**
	 * 
	 * @return
	 */
	public static Main getInstance() {

		if (myself == null) {
			myself = new Main();
		}

		return myself;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Main app = Main.getInstance();

		app.configureLogger();

		// Let's load the properties
		app.loadConfiguration();

		// Let's start the web service
		app.createService();

		app.configureSystemTray();
	}

}
