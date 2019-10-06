package com.asisoft.caen.host.connector;

import gnu.io.CommPortIdentifier;

import java.math.BigInteger;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.caen.RFIDLibrary.CAENRFIDException;
import com.caen.RFIDLibrary.CAENRFIDLogicalSource;
import com.caen.RFIDLibrary.CAENRFIDPort;
import com.caen.RFIDLibrary.CAENRFIDReader;
import com.caen.RFIDLibrary.CAENRFIDTag;

/**
 * 
 * @author "David Figueroa"
 * 
 */
public class CaenController implements Runnable {

	static Logger log = Logger.getLogger(CaenController.class);

	/**
	 * Serial port the reader is attached to
	 */
	private String serialPort;

	/**
	 * Reader's logical source to query
	 */
	private String logicalSource;

	/**
	 * Power to be used in this reader
	 */
	private int readerPower;

	/**
	 * Connection status of the controller to the reader
	 */
	private boolean initialized = false;
	private boolean finished = false;

	/**
	 * Reader from caen api
	 */
	private CAENRFIDReader reader;

	/**
	 * Creates the CAEN controller reader with the needed configuration
	 * parameters
	 * 
	 * @param serialPort
	 *            The serial port at which the reader is connected
	 * @param logicalSource
	 *            The logical source (antenna) to connect. In the case of CAEN
	 *            Slate readers there is only one logical source called
	 *            "Source_0"
	 * @param readerPower
	 *            The power in mW that will be used by the reader
	 */
	public CaenController(String serialPort, String logicalSource, int readerPower) {
		this.serialPort = serialPort;
		this.logicalSource = logicalSource;
		this.readerPower = readerPower;
	}

	/**
	 * 
	 */
	private void loopUntilConnected() {

		while (!initialized) {

			log.debug("Trying to connect to the reader ");

			try {
				Thread.sleep(1000);
				initialize();

			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Initializes the communication with the reader to send commands. First of
	 * all, tries to connect to the configured port, if not then iterates over
	 * the serial ports trying to connect
	 * 
	 * @throws Exception
	 */
	private void initialize() throws Exception {

		if (reader != null) {

			try {
				reader.Disconnect();

			} catch (CAENRFIDException e) {
				String message = "Error initializing reader";

				log.error("initialize: " + message, e);

				throw new Exception(message, e);
			}

			reader = null;
		}

		reader = new CAENRFIDReader();

		// Trying to connect to the configured port
		try {
			reader.Connect(CAENRFIDPort.CAENRFID_RS232, serialPort);
			initialized = true;

		} catch (CAENRFIDException e) {
			initialized = false;
		}

		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();

		// If not, then iterate over the serial ports until it get connected
		while (portEnum.hasMoreElements() && !initialized) {
			CommPortIdentifier portIdentifier = portEnum.nextElement();

			if (portIdentifier.getPortType() != CommPortIdentifier.PORT_SERIAL) {
				continue;
			}

			log.debug("loopUntilReconnect: trying to connect to port, " + portIdentifier.getName());

			// This connect doesn't throw exception because getPortIdentifiers()
			// actually returns the connectable ports
			reader.Connect(CAENRFIDPort.CAENRFID_RS232, portIdentifier.getName());

			initialized = isConnected();
		}

		// Setting the power to be used by the reader;
		setPower(readerPower);
		log.info("Power reported by the reader: " + getPower());

		log.info("Firmware release: " + reader.GetFirmwareRelease());
	}

	/**
	 * Returns the current connection status of the reader
	 * 
	 * @return
	 */
	public boolean isConnected() {
		try {

			if (reader != null) {
				reader.GetFirmwareRelease();
				return true;
			}

			return false;

		} catch (CAENRFIDException e) {
			return false;
		}
	}

	/**
	 * Disconnects from the reader
	 */
	public void disconnect() {

		if (reader != null) {
			try {
				reader.Disconnect();

			} catch (CAENRFIDException e) {
				log.warn("Exception trying to disconnect: " + e.getMessage());
			}
		}
		initialized = false;
		finished = true;
	}

	/**
	 * Gets the current inventory of tags visible from the reader
	 * 
	 * @return
	 */
	public String[] getInventory() throws Exception {

		try {
			CAENRFIDLogicalSource source = reader.GetSource(logicalSource);

			CAENRFIDTag[] inventoryTags = source.InventoryTag();

			String[] inventory = new String[inventoryTags.length];

			int i = 0;
			for (CAENRFIDTag tag : inventoryTags) {
				byte[] tagId = tag.GetId();
				inventory[i++] = getHex(tagId);
			}

			return inventory;

		} catch (Exception e) {

			initialized = false;

			throw new Exception(e.getMessage(), e);
		}
	}

	/**
	 * This method sets the RF power expressed in mW
	 * 
	 * @param power
	 *            the power to be set to the reader expressed in mW, goes from a
	 *            range of 0 to 199
	 */
	public void setPower(int power) {

		if (reader != null) {

			try {

				reader.SetPower(power);
				log.info("setPower: the power set to the reader is " + power);

			} catch (CAENRFIDException e) {

				log.warn("Exception trying to set reader power: " + e.getMessage());
			}
		}
	}

	/**
	 * This method gets the current setting of the RF power expressed in mW. As
	 * power levels cannot be set by readpoint, returns the power level of the
	 * reader. Variables readPointName and normalize are not used.
	 * 
	 * The power level returned by the reader goes from 0 to 199
	 * 
	 * @return
	 */
	public int getPower() {

		int power = -1;

		if (reader != null) {

			try {

				power = reader.GetPower();

			} catch (CAENRFIDException e) {

				log.warn("Exception trying to get reader power: " + e.getMessage());
			}
		}

		return power;
	}

	/**
	 * Returns the hexadecimal representation of the tagid
	 * 
	 * @param raw
	 * @return
	 */
	public static String getHex(byte[] raw) {

		if (raw == null) {
			return null;
		}

		return new BigInteger(raw).toString(16).toUpperCase();
	}

	/**
	 * 
	 */
	public void run() {

		while (!finished) {

			try {
				Thread.sleep(2000);

			} catch (InterruptedException e) {
			}

			if (!isConnected()) {
				initialized = false;
				loopUntilConnected();
			}
		}
	}
}
