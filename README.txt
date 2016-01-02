NOTE: This is an experimental research fork of an old version fo the projectfloodlight OpenFlow controller that allows for master/slave configuration.

This experimental research code that is hasn't been thoroughly tested and should not be used as the basis for any serious work. You're welcome to use the code to reproduce or continue scientific experiments.
  
To run two instances of Floodlight, one as master and one as slave, run the following scripts in the root directory.
./fl-server.sh
./fl-client.sh  
fl-server will start the master and fl-client will start the slave. 

fl-server is configured to listen at port 10000 while fl-client is configured to listen on port 10001. Feel free to change this at floodlight-sync-proto/src/main/resources/fl-client.properties

For bifrost, the following configuration should work. (in bifrost/properties)
LLDPKeepAlive=5
restport=9876
loglevel=debug
masterproxyport=6633
masteraddress=127.0.0.1
masterport=10000
slaveproxyport=6634
slaveaddress=127.0.0.1
slaveport=10001

Also included is a Mininet configuration, equipped with two remote controllers, listening at port 6633 and 6634. 
To run: sudo python internet2.py

-------

  Floodlight
  An Apache licensed, Java based OpenFlow controller

Floodlight is a Java based OpenFlow controller originally written by David Erickson at Stanford
University. It is available under the Apache 2.0 license.

For documentation, forums, issue tracking and more visit:

      http://www.openflowhub.org/display/Floodlight/Floodlight+Home
