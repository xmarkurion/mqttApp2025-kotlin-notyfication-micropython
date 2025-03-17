from markurion.internal import Led
from time import sleep

class Wifi():
    def __init__(self, led=Led()):
        self.button_state = 0
        self.led = led
        self.led.blink(1,0.1)
        self.ssid = ""
        self.password = ""
        self.IP = ""
        self.MASK = ""
        self.GATE = ""
        self.DNS = ""    
        
    def setSSID(self, ssid):
        self.ssid = ssid
    
    def setPASSWORD(self, password):
        self.password = password

    def connect(self):
        if (len(self.ssid) == 0 ):
            print(f"Please set SSID .setSSID(\"your SSID\")")
            return False
        
        if (len(self.password) == 0 ):
            print(f"Please set pass .setPASSWORD(\"your password\")")
            return False
        
        import network
        q = 0
        sta_if = network.WLAN(network.STA_IF)
        if not sta_if.isconnected():
            sta_if.active(True)
            sta_if.connect(self.ssid,self.password)
            while not sta_if.isconnected():
                if(q >= 6):
                    return 0
                if(q <= 5):
                    print(f"Can't connect retry ..{q}")
                    self.led.blink(2,0.1)
                    sleep(1) 
                q += 1
                pass # wait till connection
        self.IP, self.MASK, self.GATE, self.DNS = sta_if.ifconfig()
        print("\n")
        print(f"{'Network':_^23}")
        print(f"{'IP': <6}{self.IP}\n{'MASK': <6}{self.MASK}\n{'Gate': <6}{self.GATE}\n{'Dns': <6}{self.DNS}")
        print(f"\n{'Connected':_^23}")
        self.led.blink(2,1)
        return 1
            
if __name__ == "__main__":
    network = Wifi()
    network.setSSID("SSID")
    network.setPASSWORD("PASSWORD")
    network.connect()

