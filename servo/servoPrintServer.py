import socket
import os
import sys
import time
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

potSim = 0

while True:
    # Wait for a connection
    connection, client_address = sock.accept()

    try:
        while True:
            #potAngle = 180*ADC.read("AIN1")
            #potAngle = GPIO.input("P8_14")*180;
            #potPercept = "pot(" + str(potAngle) + ")"
            #print >>sys.stderr, potPercept
            data = connection.recv(1024)
            if data:
                #print >>sys.stderr, data
                if '!setServo' in decodeSock(data,JAVAPORT):
                #os.system(led0_on)
                    inAngleStr = decodeSock(data,JAVAPORT)[10:-1]
                    print >>sys.stderr, 'setting servo ' + inAngleStr
                    connection.sendall(encodeSock(data, JAVAPORT))
                    if potSim >= 180:
                        potSim = 0
                    else:
                        potSim = potSim + 30

                if decodeSock(data,JAVAPORT) == "*":
                    potPercept = "pot(" + str(potSim) + ")"
                    connection.sendall(encodeSock(potPercept, JAVAPORT))

            else:
                print >>sys.stderr, 'no action'
                break

    finally:
        # Clean up the connection
        connection.close()
