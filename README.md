# Bereshit
Bereshit spaceship in Java 
The main purpose at this project, is try to land Bereshit spaceship on the moon.
We tried to modeling the spaceship's sensors, we worked with PID controller on vertical & horizontal speed.


Engine:
Class engine describes the engines of the spaceship, 1 main engine and 8 secondary engines 
The main engine power is 430 Newton.
The secondary engines power is 25 Newton.


Moon:
Moon class is  describes the moon range (atmosphare) and acceleration with ratio to the moon-gravity.


PID:
PID clsas desribes the PID controller, with update and constrain methods.


Rotation:
Enum class that represents the spaceship's rotation (left, right, none)


State:
Enum class that represents the spaceship's state (orientation, braking, descent)


Ship:
Ship class represents the ship, with all the modeling of the sensors and updates functions.
Push requests methods are the movement controller


Main:
At the main class we made a while loop that run until the altitude is 0 (landed on the moon).
We updating the sensors and statements all over the loop.




Authors:
Omri Yonatani and Ido Guzi
