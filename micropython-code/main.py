from markurion.wireless import Wifi
from markurion.internal import Led
from markurion.safe import Env
from markurion.apds_helper import ApdsHelper
from markurion.images import Images
from markurion.oled_helper import Oled_Helper

from ssd1306 import SSD1306_I2C as Oled
from umqtt.simple import MQTTClient
from time import sleep
import time

#The gesture sensor
from machine import Pin, I2C
from apds9960 import uAPDS9960 as APDS9960

# Clear ram
gc.collect()
gc.threshold(gc.mem_free() // 4 + gc.mem_alloc())

# The time active 
start_time = time.time()

# Global variables
bus = machine.I2C(0,scl=machine.Pin(5), sda=machine.Pin(4))  #I2C Bus 0 
pir = machine.Pin(9, Pin.IN, Pin.PULL_DOWN)  # PIR Pin Input
red = machine.Pin(6, machine.Pin.OUT)    # Led Output
red.off()
yellow = machine.Pin(7, machine.Pin.OUT) # Led Output
yellow.off()
green = machine.Pin(8, machine.Pin.OUT)  # Led Output
green.off()

#Pinout END

env = Env()  # Passwords 
led = Led()  # Internal led operation

display = Oled(128,64, bus)  # Oled display
oled = Oled_Helper(display) # Oled class instance ( Oled_Helper )
img = Images()

apds = APDS9960(bus)        # Gesture/Light sensor init         
apds.setProximityIntLowThreshold(255) # Set activation treshold
apds.enableGestureSensor() # Gesture sensor
apds.enableLightSensor()   # Light sensor

# Show startup logo
yellow.on()
oled.displayImage(img.my_logo())
sleep(0.5)
oled.clear()

# Starting network section
oled.text(f"{'Connecting': ^16}", 0, 0)
oled.text(f"{'to WI-FI': ^16}", 0, 16)

connected = 0
while True:
    network = Wifi(led)
    network.setSSID(env.SSID)
    network.setPASSWORD(env.WPASS)
    status = network.connect()
    if(status == 1):
        print(f"\n{'OK':-^16}")
        connected = 1
        sleep(0.5)
        break

oled.text(f"{'READY': ^16}", 0, 42)
yellow.off()
green.on()
print(f"{'READY':-^16}")
sleep(1)
green.off()

# Main part of the program ----
   

#Main Board MQTT Name will be in topic
board_name = f"hire_me_please"

#topic1 = b'$SYS/broker/uptime'
topic2 = f"{board_name}/led/#"
topic3 = f"{board_name}/config/#"

yellow_led_message = "YELLOW"
green_led_message = "GREEN"
red_led_message = "RED"

def sub_cb(topic, msg):
    global yellow_led_message
    global green_led_message
    global red_led_message
    
    print(msg, topic)
    decoded_msg =  msg.decode("utf-8")
    decoded_topic = topic.decode("utf-8")
    print(decoded_msg, decoded_topic)
    
    if(decoded_topic == f"{board_name}/led/red"):
        if (decoded_msg == "1"):
            oled.text(f"{'RED ALERT ON': ^16}", 0, 32)
            oled.text(f"{red_led_message: ^16}", 0, 48)
            red.on()
            sleep(3)
        elif (decoded_msg == "0"):
            oled.text(f"{'RED ALERT OFF': ^16}", 0, 32)
            red.off()
        sleep(0.2)
    
    if(decoded_topic == f"{board_name}/led/yellow"):
        if (decoded_msg == "1"):
            oled.text(f"{'YELLOW ALERT ON': ^16}", 0, 32)
            oled.text(f"{yellow_led_message: ^16}", 0, 48)
            yellow.on()
            sleep(3)
        elif (decoded_msg == "0"):
            oled.text(f"{'YELLOW ALERT OFF': ^16}", 0, 32)
            yellow.off()
        sleep(0.2)
    
    if(decoded_topic == f"{board_name}/led/green"):
        if (decoded_msg == "1"):
            oled.text(f"{'GREEN ALERT ON': ^16}", 0, 32)
            oled.text(f"{green_led_message: ^16}", 0, 48)
            green.on()
            sleep(3)
        elif (decoded_msg == "0"):
            oled.text(f"{'GREEN ALERT OFF': ^16}", 0, 32)
            green.off()
        sleep(0.2)
        
    if(decoded_topic == f"{board_name}/led/reset"):
        green.off()
        red.off()
        yellow.off()
        
    if(decoded_topic == f"{board_name}/config/set"):
        print("Recived Screen config from MQTT")
        
        split_values = decoded_msg.split(',')
        if len(split_values) != 3:
            print("Wrong config please use 16 perval values green_msg, yellow_msg, red_msg")
        else:
            green_led_message = split_values[0]
            print(f"Setting GREEN to {green_led_message}")
            yellow_led_message = split_values[1]
            print(f"Setting YELLOW to {yellow_led_message}")
            red_led_message = split_values[2]
            print(f"Setting RED to {red_led_message}")
               
c = MQTTClient(env.ID, env.SERVER, env.PORT, user=None, password=None,keepalive=121)
c.set_callback(sub_cb)

while True:
    try:
        print(f"Trying to connect to MQTT...")
        c.connect()
        break
    except Exception as e:
        print(type(e))    # the exception instance
        print(e.args)     # arguments stored in .args
        print(e)          # __str__ allows args to be printed directly,
        sleep(2)


#c.subscribe(topic1)
c.subscribe(topic2)
c.subscribe(topic3)

config = "0"
c.publish(f"{board_name}/config", config)

def blinkme():
    led.blink(3,0.1)
    oled.clear()

counter = 0
global_light = 0
global_pir_delay = 0

def keepMeAlive():
    global global_light
    global counter
    global start_time
    global last_reading
    global global_pir_delay
    
    #Clear screen to display data
    oled.clear()
    
    #Check pir sensor
    if(pir.value()):
        global_pir_delay += 1
        if(global_pir_delay == 1):
            # This is done so only one message will be sent with each pir activation
            print("Sending PIR MQTT MESSAGE")
            c.publish(f"{board_name}/pir", str(global_pir_delay))
    else:
        #IF movement is not seen it will switch back variable to 0
        global_pir_delay = 0
    
    #Publish light value
    light = apds.readAmbientLight()
    oled.text(f"Light: {light}", 0, 0)
    if ((global_light + 10) < light or (global_light - 10) > light):
        print(f"global light {global_light} -- normal light {light}")
        print(f"Pub: {board_name}/light with value of {light}")
        c.publish(f"{board_name}/light", str(light))
        global_light = light
    
    #Check gesture section
    if apds.isGestureAvailable():
        g = apds.readGesture()
        if g is not None:
            if(g == 0):
                print("far")
                c.publish(f"{board_name}/action", "far")
                oled.displayImage(img.far())
                blinkme()
            elif(g == 1):
                print("right")
                c.publish(f"{board_name}/action", "right")
                oled.displayImage(img.right())
                blinkme()
                
            elif(g == 2):
                print("left")
                c.publish(f"{board_name}/action", "left")
                oled.displayImage(img.left())
                blinkme()
            elif(g == 3):
                print("down")
                c.publish(f"{board_name}/action", "down")
                oled.displayImage(img.down())
                blinkme()
            elif(g == 4):
                print("up")
                c.publish(f"{board_name}/action", "up")
                oled.displayImage(img.up())
                blinkme()

    if(counter < 120):
        counter += 1
        print(counter)
    else:
        tm = str(time.time() - start_time)
        oled.text(f"Alive: {tm}", 0, 32)
        print(f"Publishing time {tm}")
        c.publish(f"{board_name}/alive", tm)
        counter = 0
        
        
# SUPER MAIN MAIN LOOP
while True:
    try:
        keepMeAlive()
        c.check_msg()
        sleep(0.5)
        gc.collect()
    except Exception as e:
        print(type(e))    # the exception instance
        print(e.args)     # arguments stored in .args
        print(e)          # __str__ allows args to be printed directly,
        sleep(1)
