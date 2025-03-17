from time import sleep
import machine
from apds9960.const import *
from apds9960 import uAPDS9960 as APDS9960

#This class is helper for APDS9960 with gesture capapilities

#Usage
# x = Apds(4,5)    4 sda pin... 5 scl pin

class ApdsHelper():
    def __init__(self, sda=4, scl =5):
        self.sda_pin = sda
        self.scl_pin = scl
        self.bus = None
        self.set_bus()
        self.apds = APDS9960(self.bus)
        self.gesture_sensor_enable = False
        self.ambient_sensor_enable = False
        self.proximity_sensor_enable = False
    
    # Proximity sensor section
    def enable_proximity_sensor(self):
        self.proximity_sensor_enable = True
        self.apds.enableProximitySensor()
        
    def read_proximity(self):
        if not ( self.proximity_sensor_enable ):
            raise ValueError('Proximity sensor not enabled, Enable aproximity sensor first!')
        
        else:
            return self.apds.readProximity()
    # -----------------------------------------------
    
    # Ambient sensor section
    def enable_ambient_sensor(self):
        self.ambient_sensor_enable = True
        self.apds.enableLightSensor()
        
    def read_ambient(self):
        if not ( self.ambient_sensor_enable ):
            raise ValueError('Ambient sensor not enabled, Enable ambient sensor first!')
        else:
            return self.apds.readAmbientLight()
    # -----------------------------------------------
    
    # Gesture sensor section 
    def enable_gesture_sensor(self):
        self.gesture_sensor_enable = True
        self.apds.setProximityIntLowThreshold(50)
        self.apds.enableGestureSensor()
        self.dirs = {
            APDS9960_DIR_NONE: "none",
            APDS9960_DIR_LEFT: "left",
            APDS9960_DIR_RIGHT: "right",
            APDS9960_DIR_UP: "up",
            APDS9960_DIR_DOWN: "down",
            APDS9960_DIR_NEAR: "near",
            APDS9960_DIR_FAR: "far",
        }
        
    def set_bus(self):
        self.bus = machine.I2C(0,scl=machine.Pin(self.scl_pin), sda=machine.Pin(self.sda_pin))
    
    #This returns motion name in a string format
    def gestureCheck(self):
        if not ( self.gesture_sensor_enable ):
            raise ValueError('Gesture sensor not enabled, Enable prox sensor first!')
    
        elif self.apds.isGestureAvailable():
            motion = self.apds.readGesture()
#             readable_motion = self.dirs.get(motion, "unknown")
            return motion
    # -----------------------------------------------
    # -----------------------------------------------
    # END OF CLASS
            
if __name__ == "__main__":
    print("Testing APDS9960")
    x = ApdsHelper()
    x.enable_gesture_sensor()
#     x.enable_ambient_sensor()
#     x.enable_proximity_sensor()
     
     #Best only to enable only what you need. Buggy if all enabled.
     
    while True:
        sleep(0.5)
        
        g = x.gestureCheck()
        print(g)
        
#         print(x.read_ambient())
#         
#         print(x.read_proximity())
