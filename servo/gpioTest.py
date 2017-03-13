import Adafruit_BBIO.GPIO as GPIO
import time
import sys

GPIO.setup("P8_14", GPIO.IN)

while True:
    value = GPIO.input("P8_14")
    print >>sys.stderr, value
    time.sleep(2) # delays for 5 seconds
