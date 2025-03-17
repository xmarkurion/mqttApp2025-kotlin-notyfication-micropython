# My liblary to work with HC-SR501 movement sensor
# More about sensor https://components101.com/sensors/hc-sr501-pir-sensor

# 2 modes of working
# Repeatable(H) mode  / Non- Repeatable(L) mode

#Calibrate itself for few minutes, 2 minutes

# Library
from machine import Pin
from time import sleep
import utime

class Pir():
    def __init__(self, pin = 9):
        self.p = Pin(pin, Pin.IN, Pin.PULL_DOWN)
        self.start_time = utime.time()
        self.calibrate_time = 120
        self.calibrate = False
   
    def calibrated(self):
       if(utime.time() - self.start_time < self.calibrate_time):
           self.calibrate = True
           
    def read(self):
        if (self.calibrate):
            return p.value()
        
if __name__ == "__main__":
    print("PIR TEST")
    
    pir = Pir()
    print(pir.read())
      

