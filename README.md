# detection

System for sending messages to craft when detected in (or out) of some composite region defined with a Shapefile or for sending emails to specific recipients using templates.

Augmented JPA classes generated by [xuml-tools](https://github.com/davidmoten/xuml-tools).

## Class Diagram

<img src="https://raw.githubusercontent.com/amsa-code/detection/master/src/docs/detection-class-diagram.png"/>

State diagram for *Region Craft*:

<img src="https://raw.githubusercontent.com/amsa-code/detection/master/src/docs/region-craft-state-diagram.png"/>

Generate state diagram by running

* `StateDiagramViewerMain.java` in `src/test/java/` in your IDE
or
* `mvn exec:java -P state` from the command line.
