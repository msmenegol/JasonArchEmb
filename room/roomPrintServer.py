import socket
import os
import sys
import time
import random
#import Adafruit_BBIO.GPIO as GPIO
#import Adafruit_BBIO.ADC as ADC
#import Adafruit_BBIO.PWM as PWM

#GPIO
#GPIO.setup("P8_14", GPIO.IN)

#ADC
#ADC.setup()
#"AIN1", "P9_40"

#SERVO
#servo_pin = "P9_14"
#duty_min = 3
#duty_max = 14.5
#duty_span = duty_max - duty_min

#PWM.start(servo_pin, (100-duty_min), 60.0)

#SERVER
JAVAPORT = 6969

def decodeSock(str, port):
    return {
        JAVAPORT : str[:-1] #JAVAPORT
    }[port]

def encodeSock(str, port):
    return {
        JAVAPORT : str + '\n'#JAVAPORT
    }[port]

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# Bind the socket to the port
server_address = ('localhost', JAVAPORT)
#print >>sys.stderr, 'starting up on %s port %s' % server_address
sock.bind(server_address)
# Listen for incoming connections
sock.listen(1)

lastTime = time.time()
pplIn = False   #are there people inside?
sunLight = 0    #sunlight intensity 0-100
light = 'off';
rain = False    #is it raining outside?
window = 'closed' #state of the window


while True:
    # Wait for a connection
    connection, client_address = sock.accept()

    try:
        while True:
            #change enironment every 5 seconds
            if(time.time()-lastTime > 1):
                lastTime = time.time()
                rdm = int(random.random()*3)
                if rdm == 0:
                    pplIn = not pplIn
                if rdm == 1:
                    sunLight = random.random()*100
                if rdm == 2:
                    rain = not rain

            data = connection.recv(1024)
            if data:
                #print >>sys.stderr, data
                if '!closeWindow' in decodeSock(data,JAVAPORT):
                    print('closing window')
                    window = 'closed'
                    connection.sendall(encodeSock(data, JAVAPORT))

                if '!openWindow' in decodeSock(data,JAVAPORT):
                    print('opening window')
                    window = 'open'
                    connection.sendall(encodeSock(data, JAVAPORT))

                if '!turnLightOn' in decodeSock(data,JAVAPORT):
                    print('turning light on')
                    light = 'on'
                    connection.sendall(encodeSock(data, JAVAPORT))

                if '!turnLightOff' in decodeSock(data,JAVAPORT):
                    print('turning light off')
                    light = 'off'
                    connection.sendall(encodeSock(data, JAVAPORT))

                if decodeSock(data,JAVAPORT) == "*":
                    percept = 'peopleIn(' + str(pplIn).lower() + ');' + \
                                'sunLight(' + str(sunLight) + ');' + \
                                'light(' + light + ');' + \
                                'rain(' + str(rain).lower() + ');' + \
                                'window(' + window + ')'
                    connection.sendall(encodeSock(percept, JAVAPORT))

            else:
                print >>sys.stderr, 'no action'
                break

    finally:
        # Clean up the connection
        connection.close()
