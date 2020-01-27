# MVP implementation
The presentation layer of the app that also includes the activities has been implemented by following the MVP-pattern as shown [here](https://github.com/antoniolg/androidmvp)

Each View generally identifies one or more fragments or activities, exposing different methods that are implemented by them. Views interact with the other application layers only through their Presenters.

A Presenter takes trace of a View and of an Interactor implementing specific methods for the last one. In particular it can generally perform actions from and to the user interface and to and from the business-logic layer.

The Interactor directly acts as the point of access to the business-logic layer implmenting if needed part of the logic needed to perform or start certain actions (mostly authentication procedures and/or asynctask lanches and/or services requests).

This implementation grants a complete detachment between the presentation and the business-logic layers.
