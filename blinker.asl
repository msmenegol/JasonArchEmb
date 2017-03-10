/* Plans */
!onLed.

+!onLed
  <-  ledOn;
      .wait(500);
      !offLed.

+!offLed
  <-  ledOff;
      .wait(500);
      !onLed.
