package no.nordicsemi.android.error;

import no.nordicsemi.android.dfu.DfuBaseService;
/* loaded from: classes.dex */
public class GattError {
    public static String parseConnectionError(int i) {
        return i != 0 ? i != 1 ? i != 8 ? i != 19 ? i != 22 ? i != 34 ? i != 62 ? i != 133 ? i != 256 ? "UNKNOWN (" + i + ")" : "GATT CONN CANCEL " : "GATT ERROR" : "GATT CONN FAIL ESTABLISH" : "GATT CONN LMP TIMEOUT" : "GATT CONN TERMINATE LOCAL HOST" : "GATT CONN TERMINATE PEER USER" : "GATT CONN TIMEOUT" : "GATT CONN L2C FAILURE" : "SUCCESS";
    }

    public static String parse(int i) {
        if (i != 26) {
            if (i != 30) {
                if (i != 34) {
                    if (i != 42) {
                        if (i != 257) {
                            if (i != 58) {
                                if (i != 59) {
                                    switch (i) {
                                        case 1:
                                            return "GATT INVALID HANDLE";
                                        case 2:
                                            return "GATT READ NOT PERMIT";
                                        case 3:
                                            return "GATT WRITE NOT PERMIT";
                                        case 4:
                                            return "GATT INVALID PDU";
                                        case 5:
                                            return "GATT INSUF AUTHENTICATION";
                                        case 6:
                                            return "GATT REQ NOT SUPPORTED";
                                        case 7:
                                            return "GATT INVALID OFFSET";
                                        case 8:
                                            return "GATT INSUF AUTHORIZATION";
                                        case 9:
                                            return "GATT PREPARE Q FULL";
                                        case 10:
                                            return "GATT NOT FOUND";
                                        case 11:
                                            return "GATT NOT LONG";
                                        case 12:
                                            return "GATT INSUF KEY SIZE";
                                        case 13:
                                            return "GATT INVALID ATTR LEN";
                                        case 14:
                                            return "GATT ERR UNLIKELY";
                                        case 15:
                                            return "GATT INSUF ENCRYPTION";
                                        case 16:
                                            return "GATT UNSUPPORT GRP TYPE";
                                        case 17:
                                            return "GATT INSUF RESOURCE";
                                        default:
                                            switch (i) {
                                                case 128:
                                                    return "GATT NO RESOURCES";
                                                case 129:
                                                    return "GATT INTERNAL ERROR";
                                                case 130:
                                                    return "GATT WRONG STATE";
                                                case 131:
                                                    return "GATT DB FULL";
                                                case 132:
                                                    return "GATT BUSY";
                                                case 133:
                                                    return "GATT ERROR";
                                                case 134:
                                                    return "GATT CMD STARTED";
                                                case 135:
                                                    return "GATT ILLEGAL PARAMETER";
                                                case 136:
                                                    return "GATT PENDING";
                                                case 137:
                                                    return "GATT AUTH FAIL";
                                                case 138:
                                                    return "GATT MORE";
                                                case 139:
                                                    return "GATT INVALID CFG";
                                                case 140:
                                                    return "GATT SERVICE STARTED";
                                                case 141:
                                                    return "GATT ENCRYPTED NO MITM";
                                                case 142:
                                                    return "GATT NOT ENCRYPTED";
                                                case 143:
                                                    return "GATT CONGESTED";
                                                default:
                                                    switch (i) {
                                                        case 253:
                                                            return "GATT CCCD CFG ERROR";
                                                        case 254:
                                                            return "GATT PROCEDURE IN PROGRESS";
                                                        case 255:
                                                            return "GATT VALUE OUT OF RANGE";
                                                        default:
                                                            switch (i) {
                                                                case 4096:
                                                                    return "DFU DEVICE DISCONNECTED";
                                                                case 4097:
                                                                    return "DFU FILE NOT FOUND";
                                                                case 4098:
                                                                    return "DFU FILE ERROR";
                                                                case 4099:
                                                                    return "DFU NOT A VALID HEX FILE";
                                                                case DfuBaseService.ERROR_FILE_IO_EXCEPTION /* 4100 */:
                                                                    return "DFU IO EXCEPTION";
                                                                case DfuBaseService.ERROR_SERVICE_DISCOVERY_NOT_STARTED /* 4101 */:
                                                                    return "DFU SERVICE DISCOVERY NOT STARTED";
                                                                case DfuBaseService.ERROR_SERVICE_NOT_FOUND /* 4102 */:
                                                                    return "DFU CHARACTERISTICS NOT FOUND";
                                                                default:
                                                                    switch (i) {
                                                                        case DfuBaseService.ERROR_INVALID_RESPONSE /* 4104 */:
                                                                            return "DFU INVALID RESPONSE";
                                                                        case DfuBaseService.ERROR_FILE_TYPE_UNSUPPORTED /* 4105 */:
                                                                            return "DFU FILE TYPE NOT SUPPORTED";
                                                                        case DfuBaseService.ERROR_BLUETOOTH_DISABLED /* 4106 */:
                                                                            return "BLUETOOTH ADAPTER DISABLED";
                                                                        case DfuBaseService.ERROR_INIT_PACKET_REQUIRED /* 4107 */:
                                                                        case DfuBaseService.ERROR_FILE_SIZE_INVALID /* 4108 */:
                                                                            return "DFU INIT PACKET REQUIRED";
                                                                        case DfuBaseService.ERROR_CRC_ERROR /* 4109 */:
                                                                            return "DFU CRC ERROR";
                                                                        case DfuBaseService.ERROR_DEVICE_NOT_BONDED /* 4110 */:
                                                                            return "DFU DEVICE NOT BONDED";
                                                                        default:
                                                                            return "UNKNOWN (" + i + ")";
                                                                    }
                                                            }
                                                    }
                                            }
                                    }
                                }
                                return "GATT UNACCEPT CONN INTERVAL";
                            }
                            return "GATT CONTROLLER BUSY";
                        }
                        return "TOO MANY OPEN CONNECTIONS";
                    }
                    return "HCI ERROR DIFF TRANSACTION COLLISION";
                }
                return "GATT CONN LMP TIMEOUT";
            }
            return "HCI ERROR INVALID LMP PARAM";
        }
        return "HCI ERROR UNSUPPORTED REMOTE FEATURE";
    }

    public static String parseDfuRemoteError(int i) {
        int i2 = i & 3840;
        if (i2 != 256) {
            if (i2 != 512) {
                if (i2 != 1024) {
                    if (i2 == 2048) {
                        return SecureDfuError.parseButtonlessError(i);
                    }
                    return "UNKNOWN (" + i + ")";
                }
                return SecureDfuError.parseExtendedError(i);
            }
            return SecureDfuError.parse(i);
        }
        return LegacyDfuError.parse(i);
    }
}
