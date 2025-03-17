from machine import Pin, I2C
from ssd1306 import SSD1306_I2C as Oled
from markurion.images import Images

from time import sleep

class Oled_Helper():
    def __init__(self, display):
        self.display = display
    
    def displayImage(self, image):
        self.display.fill(0)
        self.display.blit(image, 0, 0)
        self.display.show()
    
    def clear(self):
        self.display.fill(0)
        self.display.show()
        
    def text(self, text, x, y):
        self.display.text(text,x,y)
        self.display.show()
        
    
if __name__ == "__main__":
    bus = machine.I2C(0,scl=machine.Pin(5), sda=machine.Pin(4))  # I2C Bus
    display = Oled(128,64, bus)
    img = Images()
    
    x = Oled_Helper(display)
    x.displayImage(img.up())