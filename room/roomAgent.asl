!pooling.

+!pooling : rain("true") & window("open")
    <-  closeWindow;
        .wait(100);
        !pooling.

+!pooling : window("closed") & peopleIn("true") & light("off")
    <-  turnLightOn;
        .wait(100);
        !pooling.

+!pooling : window("open") & sunLight(X) & X>50 & light("on")
    <-  turnLightOff;
        .wait(100);
        !pooling.

+!pooling : sunLight(X) & X<50 & light("off") & peopleIn("true")
    <-  turnLightOn;
        .wait(100);
        !pooling.

+!pooling : sunLight(X) & X>50 & peopleIn("true") & window("closed") & rain("false")
    <-  openWindow;
        .wait(100);
        !pooling.

+!pooling : peopleIn("false") & light("on")
    <-  turnLightOff;
        .wait(100);
        !pooling.

+!pooling
    <-  .wait(500);
        !pooling.
