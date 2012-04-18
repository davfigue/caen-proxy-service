package com.asisoft.caen.host.service;

import java.util.Arrays;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.log4j.Logger;

import com.asisoft.caen.host.connector.CaenController;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class CaenRFIDProxy {
	
	static Logger log = Logger.getLogger(CaenRFIDProxy.class);
	
	/**
	 * Controller class that intereacts with the reader
	 */
	private CaenController caenController;
	
	private Thread controllerThread;
	

	public CaenRFIDProxy(CaenController caenController) {
		
		this.caenController = caenController;
		
		this.initialize();
		
		log.info("Service created...");
	}
	
	/**
	 * Starts the communication with the reader
	 */
	private void initialize() {
		try {
			controllerThread = new Thread(caenController);
			controllerThread.start();
			
		} catch (Exception e) {
			log.error("Reader could not be initialized: " + e.getMessage());
		}
	}
	
	@WebMethod
	public String[] getInventory() {
		String[] inventory = new String[] {};
		
		try {
			inventory = caenController.getInventory();
			
			log.info("getInventory: " + Arrays.toString(inventory));
			
		} catch (Exception e) { }
		
		return inventory;
	}

	@WebMethod
	public String getVersion() {
		return "1.0";
	}
}
