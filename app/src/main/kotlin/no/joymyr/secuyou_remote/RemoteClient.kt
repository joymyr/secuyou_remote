package no.joymyr.secuyou_remote

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.secuyou.android_v22_pin_app.BleLockState.LOCKING_MECHANISM_POSITION
import com.secuyou.android_v22_pin_app.BleManagerCallbacks
import com.secuyou.android_v22_pin_app.BleMulticonnectProfileService

private val BluetoothDevice.deviceId: String
    get() = this.address.replace(":", "_")

@SuppressLint("MissingPermission")
class RemoteClient(
    val context: Context,
    val binder: BleMulticonnectProfileService.LocalBinder
): BleManagerCallbacks {
    companion object {
        const val TAG = "RemoteClient"
    }

    var mqttClient: MqttClient = MqttClient(context, ::toggleLock, ::getDevices, ::updateAllDevices)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            throw Exception("Missing required permission");
        }
    }

    private fun toggleLock(deviceId: String, locked: Boolean) {
        val device = binder.managedDevices.first { deviceId == it.deviceId }
        if (binder.getLockMechanismPosition(device) == LOCKING_MECHANISM_POSITION.LOCKED != locked) {
            binder.lock_unlock(device)
        }
    }

    private fun getDevices(): List<String> {
        return binder.managedDevices.map { bluetoothDevice -> bluetoothDevice.deviceId }
    }

    private fun updateAllDevices() {
        binder.managedDevices.forEach {
            mqttClient.onLockStateChanged(it.deviceId, binder.getLockMechanismPosition(it) == LOCKING_MECHANISM_POSITION.LOCKED, binder.getHandleValue(it) == 0, false)
            val batteryPercentage = when(binder.getBatteryValue(it)) {
                0 -> 100
                1 -> 10
                2 -> 5
                3 -> 0
                else -> 100
            }
            mqttClient.onBatteryInfo(it.deviceId, batteryPercentage)
        }
    }

    fun destroy() {
        mqttClient.close()
    }

    override fun onBonded(bluetoothDevice: BluetoothDevice?) {
        Log.d(TAG, "onBonded ${bluetoothDevice?.name}")
    }

    override fun onBondingRequired(bluetoothDevice: BluetoothDevice?) {
        Log.d(TAG, "onBondingRequired ${bluetoothDevice?.name}")
    }

    override fun onDeviceConnected(bluetoothDevice: BluetoothDevice?) {
        bluetoothDevice?.let { mqttClient.onLockAdded(it.deviceId, it.name) }
    }

    override fun onDeviceConnecting(bluetoothDevice: BluetoothDevice?) {
        Log.d(TAG, "onDeviceConnecting ${bluetoothDevice?.name}")
    }

    override fun onDeviceDisconnected(bluetoothDevice: BluetoothDevice?) {
        Log.d(TAG, "onDeviceDisconnected ${bluetoothDevice?.name}")
    }

    override fun onDeviceDisconnecting(bluetoothDevice: BluetoothDevice?) {
        Log.d(TAG, "onDeviceDisconnecting ${bluetoothDevice?.name}")
    }

    override fun onDeviceNotSupported(bluetoothDevice: BluetoothDevice?) {
        Log.d(TAG, "onDeviceNotSupported ${bluetoothDevice?.name}")
    }

    override fun onDeviceReady(bluetoothDevice: BluetoothDevice?) {
        Log.d(TAG, "onDeviceReady ${bluetoothDevice?.name}")
    }

    override fun onError(bluetoothDevice: BluetoothDevice?, str: String?, i: Int) {
        Log.d(TAG, "onError ${bluetoothDevice?.name}")
    }

    override fun onLinkLossOccur(bluetoothDevice: BluetoothDevice?) {
        Log.d(TAG, "onLinkLossOccur ${bluetoothDevice?.name}")
    }

    override fun onLockFirmwareValueReceived(bluetoothDevice: BluetoothDevice?, bArr: ByteArray?) {
        Log.d(TAG, "onLockFirmwareValueReceived ${bluetoothDevice?.name}")
    }

    override fun onLockModelValueReceived(bluetoothDevice: BluetoothDevice?, bArr: ByteArray?) {
        Log.d(TAG, "onLockModelValueReceived ${bluetoothDevice?.name}")
    }

    override fun onLockNameValueReceived(bluetoothDevice: BluetoothDevice?, bArr: ByteArray?) {
        Log.d(TAG, "onLockNameValueReceived ${bluetoothDevice?.name}")
    }

    override fun onLockSerialValueReceived(bluetoothDevice: BluetoothDevice?, bArr: ByteArray?) {
        Log.d(TAG, "onLockSerialValueReceived ${bluetoothDevice?.name}")
    }

    override fun onLockStateValueReceived(bluetoothDevice: BluetoothDevice?, bArr: ByteArray?) {
        try {
            Log.d(TAG, "onLockStateValueReceived ${bluetoothDevice?.name}, lockstate: ${binder.getStateLock(bluetoothDevice)}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLockstatusValueReceived(bluetoothDevice: BluetoothDevice?, bArr: ByteArray?) {
        try {
            bluetoothDevice?.deviceId?.let { mqttClient.onLockStateChanged(it, binder.getLockMechanismPosition(bluetoothDevice) == LOCKING_MECHANISM_POSITION.LOCKED, binder.getHandleValue(bluetoothDevice) == 0, true) }
            Log.d(TAG, "onLockstatusValueReceived ${bluetoothDevice?.name}, lockstatus: ${binder.getLockMechanismPosition(bluetoothDevice)}, handlestatus: ${binder.getHandleValue(bluetoothDevice)}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onServicesDiscovered(bluetoothDevice: BluetoothDevice?, z: Boolean) {
        Log.d(TAG, "onServicesDiscovered ${bluetoothDevice?.name}")
    }

    override fun shouldEnableBatteryLevelNotifications(bluetoothDevice: BluetoothDevice?): Boolean {
        return true;
    }

    override fun shouldEnableLockNameNotifications(bluetoothDevice: BluetoothDevice?): Boolean {
        return true;
    }

    override fun shouldEnableLockStateNotifications(bluetoothDevice: BluetoothDevice?): Boolean {
        return true;
    }

    override fun shouldEnableLockstatusNotifications(bluetoothDevice: BluetoothDevice?): Boolean {
        return true;
    }
}
