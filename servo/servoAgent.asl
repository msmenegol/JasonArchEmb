/* Plans */
pot(90).

+pot(X)
  <-  setServo(X);
      .wait(500).
