import ubinascii
import machine
class Env():
    def __init__(self):
        self.SSID = "Nope_its_not_here"
        self.WPASS = "ofcourse_i_wont_tell_you"
        self.ID = "0x{}".format(ubinascii.hexlify(machine.unique_id()).decode().upper())
        self.SERVER = "broker.emqx.io"
        self.PORT = 1883
        self.USER = ""
        self.MQTT_PASS = ""
    
        
if __name__ == "__main__":
    
    a = Env()
    for key, value in a.__dict__.items():
        print(f"{key} -> {value}")
        