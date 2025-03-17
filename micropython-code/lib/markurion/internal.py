import machine
from time import sleep

class Led():
    def __init__(self):
        self.led = machine.Pin("LED", machine.Pin.OUT)
    
    # If you want to use diferent pin this apply to Pico Pi W
    def setLedPin(self, pin):
        self.led = machine.Pin(pin, machine.Pin.OUT)
        
    def switch_LED(self,x):
        if not (x == 0):
            self.led.on()
        else:
            self.led.off()
            
    def blink(self, amount, speed):
        for x in range(amount):
            self.switch_LED(1)
            sleep(speed)
            self.switch_LED(0)
            sleep(speed)
            
if __name__ == "__main__":
    print("Internal LED class")
    led = Led()
    led.switch_LED(1)
    led.blink(2,0.5)
    

