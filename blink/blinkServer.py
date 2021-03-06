import socket
import os
import sys
import Adafruit_BBIO.GPIO as GPIO

#Command for turning led0 "On"
led0_on = 'echo 255 > /sys/class/leds/beaglebone:green:usr0/brightness'
#Command for turning led0 "Off"
led0_off = 'echo 0 > /sys/class/leds/beaglebone:green:usr0/brightness'

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

while True:
    # Wait for a connection
    connection, client_address = sock.accept()

    try:
        while True:
            data = connection.recv(1024)
            if data:
                print >>sys.stderr, data
                if decodeSock(data,JAVAPORT) == '!ledOn':
                    os.system(led0_on)
                    #print >>sys.stderr, 'on'
                if decodeSock(data,JAVAPORT) == '!ledOff':
                    os.system(led0_off)
                    #print >>sys.stderr, 'off'

                connection.sendall(encodeSock(data, JAVAPORT))
            else:
                print >>sys.stderr, 'no action'
                break

    finally:
        # Clean up the connection
        connection.close()
