import socket
import os
import sys
import time
import random
import select
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

localPorts = [JAVAPORT]
remotePorts = []

def decodeSock(str, port):
    return {
        JAVAPORT : str[:-1] #JAVAPORT
    }[port]

def encodeSock(str, port):
    return {
        JAVAPORT : str + '\n'#JAVAPORT
    }[port]

def createSocket(TCP_PORT):
    serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    serverSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    #serverSocket.setblocking(0)
    serverSocket.bind(('localhost', TCP_PORT))
    serverSocket.listen(1)

    return serverSocket


sockList = []

lastTime = time.time()

pplIn = False   #are there people inside?
sunLight = 0    #sunlight intensity 0-100
light = 'off';
rain = False    #is it raining outside?
window = 'closed' #state of the window

def buildPercept():
    percept = 'peopleIn(' + str(pplIn).lower() + ');' + \
                'sunLight(' + str(sunLight) + ');' + \
                'light(' + light + ');' + \
                'rain(' + str(rain).lower() + ');' + \
                'window(' + window + ')'

    return percept

for p in localPorts:
    sock = createSocket(p)
    clientSock, address = sock.accept()
    remotePorts.append(address[1])
    sockList.append(clientSock)

while True:

#percepts update
    if(time.time()-lastTime > 1):
        lastTime = time.time()
        rdm = int(random.random()*4)
        if rdm == 0:
            pplIn = not pplIn
        if rdm == 1:
            sunLight = random.random()*100
        if rdm == 2:
            rain = not rain

        _, writable, exceptionalW = select.select([], sockList, sockList, 0.5)
        for w in writable:
            if remotePorts[localPorts.index(JAVAPORT)] in w.getpeername():
                percept = buildPercept()
                w.sendall(encodeSock(percept, JAVAPORT))

#reads message
    readable, _ , exceptionalR = select.select(sockList, [], sockList, 0.5)

    for s in readable:

        data = s.recv(1024)
        if data:
            decodeData = decodeSock(data, JAVAPORT)
            if '!' in decodeData:

                if 'closeWindow' in decodeData:
                    print('closing window')
                    window = 'closed'

                if 'openWindow' in decodeData:
                    print('opening window')
                    window = 'open'

                if 'turnLightOn' in decodeData:
                    print('turning light on')
                    light = 'on'

                if 'turnLightOff' in decodeData:
                    print('turning light off')
                    light = 'off'

                _, writable, exceptionalW = select.select([], sockList, sockList, 0.5)
                for w in writable:
                    if remotePorts[localPorts.index(JAVAPORT)] in w.getpeername():
                        w.sendall(encodeSock(decodeData, JAVAPORT))#send action confirmation

        else:
            s.close()
            sockList.remove(s)
