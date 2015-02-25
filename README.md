# Edison

This is a Golgi demonstration application for the Intel Edison board. The project allows remote control and monitoring (C&M) of devices and sensors attached to the Edison's 
Arduino breakout board from an Android mobile device. By default the demonstration has a temperature sensor attached to pin A0 of the Arduino breakout, a potentiometer attached 
to pin A1, an LED to pin 4, a capacitative touch sensor to pin 2 and a PIR sensor to pin 12.

The server running on the Intel Edison is written in node.js and makes use of the 'galileo-io' and 'mraa' libraries.
