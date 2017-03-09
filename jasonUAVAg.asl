
/* Plans */
!start.

+!start
  <-  move(100,200,300);
      !goto(200,300,400).

+!goto(X,Y,Z)
  <-  move(X,Y,Z).
