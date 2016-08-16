# Monte-Carlo-Localization on Lego NXT Mindstorms

This is a set of classes to enable the Lego NXT Robot to make use of the Monte-Carlo-Localization algorithm from the [Artificial Intelligence - A Modern Approach 3rd Edition](http://aima.cs.berkeley.edu/) [Java implementation](https://github.com/aimacode/aima-java/tree/AIMA3e) in connection with the [leJos NXJ framework](http://www.lejos.org/nxj.php).

The implementation consists of 2 parts:

###1. [NXT daemon](/MCL_Mindstorms_NXT)

The NXT runs a daemon in the leJos runtime. This daemon awaits a bluetooth connection and process the commands which are sent from the PC.

###2. [PC controls](/MCL_Mindstorms)

The PC is controlling the NXT. It displays a GUI from which the user can control the Monte-Carlo-Localization algorithm and the robot.
All parameters regarding both algorithm and robot can be managed from this GUI.

###Additional data

In addition a few [SVG maps](/maps) for example environments included.
A model created with the Lego Digital Designer and instructions to rebuild that model can be found in [LEGO instructions](/LEGO_instructions).
