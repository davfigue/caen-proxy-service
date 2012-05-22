Caen Proxy Service
==================

The objective of this module is to provide a TCP intermediate proxy to CAEN RFID readers that are
not directly plug to the ALE server and also dont have network connectivity facilities. 

This software should be installed on the host machine where the reader is
plugged in, allows to communicate to RFID middlewares like ALE middleware by fosstrak <http://www.fosstrak.org>

For the case of fosstrack to allow the connection, I developed my own "hal-impl-caen" that is plugable into the ALE server.


Requirements
============

You should have installed the serial and parallel communication library for java RXTX for your platform
The version used in this software is: 2.2pre2

Look at <http://rxtx.qbang.org/wiki/index.php/Main_Page> for downloading and installing this library


How to use
==========

Run with: 
	$ java -jar caen-proxy-service-<version>.jar

The program will start a web service at the endpoint: "http://<ip>:<port>/caenrfid-proxy"
The default configuration is to listen in all the network interfaces, then the default endpoint address is:

	"http://0.0.0.0:8888/caenrfid-proxy"

The wsdl is located at: 
	"http://0.0.0.0:8888/caenrfid-proxy?wsdl"

The configuration can be tuned using the file in "/props/CaenProxyService_default.xml"
The only thing that I think should be changed is the "serialPort" field, the software tries to connect to the reader first 
to the port specified here but if is not possible then automatically tries to connect to any available serial port 
(it keeps trying eternally until the program quits).


Posible issues
==============

There is a known issue using this software in "Windows" when the path to the directory containing the jar has spaces.
This is related to a dynamic lookup of the absolute paths to use at the initial configuration of the service.

-> "Yes, this is going to be solved in future versions" <-

Solution: use paths (files and folders) with names containing no spaces.

