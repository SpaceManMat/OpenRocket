CD Override extension for OpenRocket
==============================================

This is an example simulation extension plugin for OpenRocket.  It defines a simulation extension that allows user to override the CD values calculated during the simulation.


Compiling and usage
-------------------

Compile by running `ant` script build.xml.  This creates the file `CDoverride.jar`.  Copy this to the OpenRocket plugin directory (`~/.openrocket/Plugins/` on Linux, `~/Library/Application Support/OpenRocket/Plugins/` on Mac, `...\Application Data\OpenRocket\ThrustCurves\Plugins\` on Windows).  Then restart OpenRocket.

You can add and configure it in the simulation edit dialog under Simulation options -> Add extension -> Flight -> CD Override.

There are 3 options to override. 
1. Using a multiplier to alter the CD
2. Using a set of 3 multipliers to alter Friction, Pressure and Base CD values 
3. Using a file to alter the CD

