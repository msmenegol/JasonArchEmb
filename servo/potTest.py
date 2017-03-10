import Adafruit_BBIO.ADC as ADC
import time
import sys


ADC.setup()

"AIN1", "P9_40"

while True:
    value = ADC.read("AIN1")
    print >>sys.stderr, value
    time.sleep(2) # delays for 5 seconds
