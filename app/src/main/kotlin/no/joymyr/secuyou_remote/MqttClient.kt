package no.joymyr.secuyou_remote

import android.content.Context
import android.util.Log
import com.google.gson.JsonParser
import info.mqtt.android.service.MqttAndroidClient
import no.joymyr.secuyou_remote.R
import org.eclipse.paho.client.mqttv3.*

class MqttClient(
    context: Context,
    toggleLock: (deviceId: String, locked: Boolean) -> Unit,
    getDevices: () -> List<String>,
    updateAllDevices: () -> Unit
) {
    var mqttUri = context.resources.getString(R.string.mqtt_uri)
    var mqttMainTopic = context.resources.getString(R.string.mqtt_main_topic)
    var mqttInclusionTopic = context.resources.getString(R.string.mqtt_inclusion_topic)
    var mqttBatteryEventTopic = context.resources.getString(R.string.mqtt_battery_event_topic)
    var mqttAlarmEventTopic = context.resources.getString(R.string.mqtt_alarm_event_topic)
    var mqttLockEventTopic = context.resources.getString(R.string.mqtt_lock_event_topic)
    var mqttLockCommandTopic = context.resources.getString(R.string.mqtt_lock_command_topic)

    var mqttUsername = context.resources.getString(R.string.mqtt_username)
    var mqttPassword = context.resources.getString(R.string.mqtt_password)

    private val client = MqttAndroidClient(context, mqttUri, "AndroidRaspberryPi")

    private var unpublished: ArrayList<Pair<String, String>> = arrayListOf()
    private var unsubscribedTopics: ArrayList<String> = arrayListOf(mqttMainTopic)

    companion object {
        const val TAG = "MqttClient"
    }

    init {
        connect(topics = unsubscribedTopics) { topic, message ->
            if (topic == mqttMainTopic) {
                Log.d(TAG, "Message to main topic: $message")
                updateAllDevices.invoke()
            } else {
                getDevices.invoke().forEach {
                    val lockCommandTopic = "$mqttLockCommandTopic/ad:s${it}_0"
                    if (lockCommandTopic == topic) {
                        val locked = JsonParser().parse(message.toString()).asJsonObject["val"].asBoolean
                        Log.d(TAG, "MQTT Set lock status: $locked")
                        toggleLock(it, locked);
                    }
                }
            }
        }
    }

    private fun connect(
        topics: ArrayList<String>? = null,
        messageCallBack: ((topic: String, message: MqttMessage) -> Unit)? = null) {
        try {
            val options = MqttConnectOptions()
            options.userName = mqttUsername
            options.password = mqttPassword.toCharArray()
            client.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String) {
                    unpublished.forEach {
                        publishMessage(it.first, it.second);
                    }
                    unpublished.clear()
                    topics?.forEach {
                        subscribeTopic(it)
                    }
                    Log.d(TAG, "Connected to: $serverURI")
                }

                override fun connectionLost(cause: Throwable) {
                    Log.e(TAG, "The Connection was lost.")
                    cause.printStackTrace()
                }

                @Throws(Exception::class)
                override fun messageArrived(topic: String, message: MqttMessage) {
                    Log.d(TAG, "Incoming message from $topic: $message")
                    messageCallBack?.invoke(topic, message)
                }

                override fun deliveryComplete(token: IMqttDeliveryToken) {
                    Log.d(TAG, "Message delivery complete")
                }
            })
            Log.d(TAG, "Connecting to: ${client.serverURI}...")
            val token = client.connect(options)
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "Connection failed")
                    exception?.printStackTrace()
                }

            }

        } catch (e: MqttException) {
            Log.e(TAG, "Failed to connect to: ${client.serverURI}")
            e.printStackTrace()
        }
    }

    fun onLockAdded(deviceId: String, deviceName: String) {
        val batteryEventTopic = "$mqttBatteryEventTopic/ad:s${deviceId}_0";
        val alarmEventTopic = "$mqttAlarmEventTopic/ad:s${deviceId}_0";
        val lockEventTopic = "$mqttLockEventTopic/ad:s${deviceId}_0";
        subscribeTopic("pt:j1/mt:cmd$lockEventTopic")
        publishMessage(
            mqttInclusionTopic, """{
                "serv": "secuyou",
                "tags": [],
                "type": "evt.thing.inclusion_report",
                "val": {
                    "address": "s$deviceId",
                    "category": "DOORLOCK",
                    "comm_tech": "zigbee",
                    "device_id": "$deviceId",
                    "groups": ["ch_0"],
                    "hw_ver": "1",
                    "manufacturer_id": "Secuyou",
                    "power_source": "battery",
                    "product_hash": "$deviceId",
                    "product_name": "$deviceName",
                    "security": "secure",
                    "services": [
                      {
                        "address": "$batteryEventTopic",
                        "enabled": true,
                        "groups": [
                          "ch_0"
                        ],
                        "interfaces": [
                          {
                            "intf_t": "out",
                            "msg_t": "evt.lvl.report",
                            "val_t": "int",
                            "ver": "1"
                          },
                          {
                            "intf_t": "out",
                            "msg_t": "evt.alarm.report",
                            "val_t": "str_map",
                            "ver": "1"
                          },
                          {
                            "intf_t": "in",
                            "msg_t": "cmd.lvl.get_report",
                            "val_t": "null",
                            "ver": "1"
                          }
                        ],
                        "location": "",
                        "name": "battery",
                        "prop_set_ref": "",
                        "props": {
                          "is_secure": false,
                          "is_unsecure": true
                        }
                      },
                      {
                        "address": "$alarmEventTopic",
                        "enabled": true,
                        "groups": [
                          "ch_0"
                        ],
                        "interfaces": [
                          {
                            "intf_t": "out",
                            "msg_t": "evt.alarm.report",
                            "val_t": "str_map",
                            "ver": "1"
                          },
                          {
                            "intf_t": "in",
                            "msg_t": "cmd.alarm.get_report",
                            "val_t": "string",
                            "ver": "1"
                          }
                        ],
                        "location": "",
                        "name": "alarm_lock",
                        "prop_set_ref": "",
                        "props": {
                          "is_secure": false,
                          "is_unsecure": true
                        }
                      },
                      {
                        "address": "$lockEventTopic",
                        "enabled": true,
                        "groups": [
                          "ch_0"
                        ],
                        "interfaces": [
                          {
                            "intf_t": "out",
                            "msg_t": "evt.lock.report",
                            "val_t": "bool_map",
                            "ver": "1"
                          },
                          {
                            "intf_t": "in",
                            "msg_t": "cmd.lock.set",
                            "val_t": "bool",
                            "ver": "1"
                          },
                          {
                            "intf_t": "in",
                            "msg_t": "cmd.lock.get_report",
                            "val_t": "string",
                            "ver": "1"
                          }
                        ],
                        "location": "",
                        "name": "door_lock",
                        "prop_set_ref": "",
                        "props": {
                          "is_secure": true,
                          "is_unsecure": false
                        }
                      }
                    ]
                },
                "ver": "1",
                "val_t": "object",
                "src": "fh-secuyou",
                "topic": "$mqttInclusionTopic"
            }""")
    }

    fun onLockStateChanged(deviceId: String, locked: Boolean, closed: Boolean, newState: Boolean) {
        val lockEventTopic = "pt:j1/mt:evt$mqttLockEventTopic/ad:s${deviceId}_0"
        publishMessage(
            lockEventTopic, """{
                "serv": "door_lock",
                "type": "evt.lock.report",
                "val_t": "bool_map",
                "val": {
                    "door_is_closed": $closed,
                    "latch_is_closed": $closed,
                    "is_secured": $locked
                },
                "src": "fh-secuyou"
            }""")
        if (newState) {
            val alarmEventTopic = "pt:j1/mt:evt$mqttAlarmEventTopic/ad:s${deviceId}_0"
            publishMessage(
                alarmEventTopic, """{
                    "serv": "alarm_lock",
                    "type": "evt.alarm.report",
                    "val_t": "str_map",
                    "val": {
                        "event": "${if (locked) "manual_lock" else "manual_unlock"}",
                        "status": "activ"
                    },
                    "src": "fh-secuyou"
                }"""
            )
        }
    }

    fun onBatteryInfo(deviceId: String, batteryPercentage: Int) {
        val lockEventTopic = "pt:j1/mt:evt$mqttBatteryEventTopic/ad:s${deviceId}_0"
        publishMessage(
            lockEventTopic, """{
                "serv": "battery",
                "type": "evt.lvl.report",
                "val_t": "int",
                "val": $batteryPercentage,
                "src": "fh-secuyou"
            }""")
    }

    fun onInfo(deviceId: String, message: String) {
//        publishMessage(
//            mqttAlarmEvtTopic, """{
//            "serv": "alarm_lock",
//            "type": "evt.alarm.report",
//            "val_t": "str_map",
//            "val": {
//                "event": "$message",
//                "status": "activ"
//            },
//            "src": "lock"
//        }""") // TODO: Send correct events
    }

    fun onError(deviceId: String, message: String) {
        val alarmEventTopic = "pt:j1/mt:evt$mqttAlarmEventTopic/ad:s${deviceId}_0"
        publishMessage(
            alarmEventTopic, """{
                    "serv": "alarm_lock",
                    "type": "evt.alarm.report",
                    "val_t": "str_map",
                    "val": {
                        "event": "lock_failed",
                        "status": "activ",
                    },
                    "src": "fh-secuyou"
                }"""
        )
    }

    private fun publishMessage(topic: String, msg: String) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            if(client.isConnected)
                client.publish(topic, message.payload, 0, true)
            else
                unpublished.add(Pair(topic, msg))
            Log.d(TAG, "$msg published to $topic")
        } catch (e: MqttException) {
            Log.d(TAG, "Error Publishing to $topic: " + e.message)
            e.printStackTrace()
        }

    }

    private fun subscribeTopic(topic: String, qos: Int = 0) {
        if (client.isConnected) {
            client.subscribe(topic, qos).actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.d(TAG, "Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.d(TAG, "Failed to subscribe to $topic")
                    unsubscribedTopics.add(topic)
                    exception.printStackTrace()
                }
            }
        } else {
            Log.d(TAG, "Client is not connected yet")
            unsubscribedTopics.add(topic)
        }
    }

    fun close() {
        client.apply {
            unregisterResources()
            close()
        }
    }
}