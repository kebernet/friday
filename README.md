Friday X10 Gateway for SmartThings
==================================


This is a server that allow bridging from SmartThings to X10 devices. It is really, really rough
and not recommended for use by anyone.

Requirements
------------

Some kind of X10 interface. I am using a CM11A, connected to a Rasberry Pi with a TRENDNet TU-S9 
serial adapter.

RXTX: 
 * Linux : apt-get install librxtx-java
 * Mac : Place the RXTX extension into /Library/Java/Extensions
 * Windows : You're on your own. Likely you just need to put the DL on your java.library.path

Configuration
-------------

Configuration is stored in /etc/friday/config.xml. See the textConfig xml doc in src/test/resource.
When running you will need to export FRIDAY_OPTS="-Djava.libray.pay=/usr/lib/jni" on Linux. 

Build
-----

gradle distZip
 
This will give you a zip file in build/distributions that you can unzip and run.

SmartThings
-----------

Publish the devices from src/main/groovy to your ST account.