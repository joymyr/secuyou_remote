# Secuyou Remote

Control Secuyou locks over MQTT, to integrate with Futurehome (and similar smart hubs).
Mostly implements Futurehome IoT Messaging Protocol https://github.com/futurehomeno/fimp-api/.
The app is made mainly for running continuously on a Raspberry PI with Android, but should also work using a spare Android phone.
It's based on Secuyou Smart Lock Android app.

* Recommends to set up the lock with the official Secuyou app first and set a pin code
* Add configuration to local.properties:
  ```
  mqttUri="tcp://xxx.xxx.x.xxx:1884"
  mqttUsername="<mqtt username>"
  mqttPassword="<mqtt password>"
  mqttMainTopic = "<Main MQTT topic for update requests>"
  mqttInclusionTopic = "<Topic for receiving door lock commands>"
  mqttLockEventTopic = "<Topic for sending door lock events>"
  mqttBatteryEventTopic = "<Topic for sending battery status>"
  mqttAlarmEventTopic = "<Topic for sending door lock errors>"
  mqttLockCommandTopic = "<Topic for receiving the door lock command>"
  ```
* Install Android on a Raspberry PI or find a spare Android phone that you can leave within reach of your lock, and always connected to power
* Run this app on the PI/phone
* Add the lock using the pin code
* The lock should now communicate over MQTT

Known issues:
* App crashes on launch because of some app verification issues, but can be opened again afterwards
* Some issues with app permissions on first app launch, especially on Android 13
* Other bugs here and there

Security considerations:
* Exposing the lock to MQTT might make it more vulnerable, especially if someone is able to access your home network
* There's no verification except for MQTT verification, so if someone gets access to your MQTT queue, 
  or you're using an unsecured MQTT queue, they can unlock your lock
* The lock might still be more secure as you can check status and receive notifications remotely

Other notes:
* It's only tested with one lock, but should work with multiple locks as long as they're in range

The MQTT messages looks like this (Tested with Futurehome):
* Command for locking/unlocking:
```
{
  "serv": "door_lock",
  "type": "cmd.lock.set",
  "val_t": "bool",
  "val": <true or false>,
  "src": "<anything other than lock>"
}
```
* Event sent from this app when the lock locks/unlocks or the door handle opens/closes:
```
{
    "serv": "door_lock",
    "type": "evt.lock.report",
    "val_t": "bool_map",
    "val": {
        "door_is_closed": <true or false>,
        "latch_is_closed": <true or false>,
        "is_secured": <true or false>
    },
    "src": "lock"
}
```
* Alarm event sent if there's an error (unable to lock):
```
{
    "serv": "alarm_lock",
    "type": "evt.alarm.report",
    "val_t": "str_map",
    "val": {
        "event": "lock_failed",
        "status": "activ",
    },
    "src": "fh-secuyou"
}
```

