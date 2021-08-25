package com.durgesh.rvoperation.controller

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.collections.HashSet


/**
 * Class Name : BLEController
 * Description : This class is used for finding/locating/discovering/searching the near by
 * BLE device based on configuration supplied by native controller
 */
class BLEController private constructor(val mContext: Context) {


    companion object {

        /**
         * Variable reference holding the reference of Device Finder class for communication from outside world
         */
        private var INSTANCE: BLEController? = null

        /**
         * Variable TAG representing the log of this class
         */
        private val TAG: String = BLEController::class.simpleName!!

        /**
         * Variable METHOD_TAG representing the log of this class
         */
        private var METHOD_TAG: String = ""

        /**
         * Variable holding activity reference
         */
        private var mContext: Context? = null

        /**
         * Set containing the list of UUID for filtering the scanning result of nearby device
         */
        val filterUUIDList = HashSet<UUID>()

        /**
         * Set containing the list of device model number or name for filtering the scanning result of nearby device
         */
        val filterDeviceModelNumberOrNameList = HashSet<String>()

        /**
         * Constant for enable BLUETOOTH request
         */
        private const val REQUEST_ENABLE_BLUETOOTH = 1000

        /**
         * ArrayList used for fetching and storing the already paired device from Bluetooth Adapter
         */
        private val pairedDeviceList: ArrayList<String?> = ArrayList()

        /**
         * BluetoothAdapter is Adapter that is actually responsible for all bluetooth related action.
         */
        private var mBluetoothAdapter: BluetoothAdapter? = null

        private var mBluetoothManager: BluetoothManager? = null


        /**
         * BluetoothLeScanner reference object for scanning device with client having os version above lolipop
         */
        private var scanner: BluetoothLeScanner? = null

        /**
         * ScanFilter arraylist for holding the filterable category
         */
        private var scanFilterList: MutableList<ScanFilter> = ArrayList()

        /**
         * ScanSettings for setting the frequency of scanning and power consumption utilization
         */
        private var scanSetting: ScanSettings? = null

        /**
         * ScanCallback is for capturing and and flushing the scan result for device discovered
         */
        private var scanCallback: ScanCallback? = null


        private var isScanning = false


        /**
         * Method Name : getInstance
         * Description : This method is used to create DeviceFinder instance if not create
         * otherwise return the same if already created.
         * @param mContext : Activity Context for performing operation
         */
        fun getInstance(mContext: Context): BLEController? {
            METHOD_TAG = "getInstance()"
            if (INSTANCE == null) {
                synchronized(BLEController::class.java)
                {
                    if (INSTANCE == null) {
                        INSTANCE = BLEController(mContext)
                        this.mContext = mContext

                    }
                }
            }
            return INSTANCE
        }


    }

    /**
     * Method Name : getBluetoothManager
     * Description : This method is used to get reference of Bluetooth Manager provided by Bluetooth Android SDK.
     *
     * @return @BluetoothManager
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun getBluetoothManager(): BluetoothManager? {
        METHOD_TAG = BLEController::getBluetoothManager.name
        if (mBluetoothManager == null) {
            try {
                if (mContext != null) {
                    mBluetoothManager =
                        mContext?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                } else {
                    Log.w(
                        TAG,
                        "[ $METHOD_TAG ] ERROR : mContext is null"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "$METHOD_TAG EXCEPTION : ${e.message}")
            }
        }
        return mBluetoothManager
    }

    /**
     * Method Name : isBlueToothSupported
     * Description : This method is used to verify does this device have bluetooth support
     *
     * @return @Boolean true represent yes ,false no
     */
    private fun isBlueToothSupported(): Boolean {
        METHOD_TAG = BLEController::isBlueToothSupported.name
        return getBluetoothManager() != null
    }

    /**
     * Method Name : isBluetoothEnabled
     * Description : This method is used to verify that is bluetooth of this device is switched ON/OFF
     *
     * @return @Boolean true represent yes ,false no
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun isBluetoothEnabled(): Boolean {
        METHOD_TAG = "[ ${BLEController::isBluetoothEnabled.name} ]"
        if (getBluetoothManager() != null) {
            if (getBluetoothAdapter() != null) {

                Log.i(
                    TAG,
                    "BluetoothAdapter Status : " + getBluetoothAdapter()?.isEnabled
                )
                return getBluetoothAdapter()?.isEnabled!!
            }
            Log.w(
                TAG,
                "Bluetooth is off"
            )


        } else {
            Log.w(
                TAG,
                "BluetoothManager is uninitialized or null"
            )

        }
        return false
    }

    /**
     * Method Name : requestBluetoothEnable
     * Description : This method is used to request to enable bluetooth of this device if it is switched ON
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun requestBluetoothEnable() {
        METHOD_TAG = "[ ${BLEController::requestBluetoothEnable.name} ]"
        if (!isBluetoothEnabled()) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (mContext is Activity)
                (mContext as Activity).startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH)
        } else {
            Log.w(
                TAG,
                "Bluetooth is not enabled"
            )


        }
    }


    /**
     * Method Name : getBluetoothAdapter
     * Description : This method is used to get reference of Bluetooth Adapter
     *
     * @return @BluetoothAdapter
     */
    fun getBluetoothAdapter(): BluetoothAdapter {
        METHOD_TAG = "[ ${BLEController::getBluetoothAdapter.name} ]"
        if (mBluetoothAdapter == null) {
            if (getBluetoothManager() != null) {
                mBluetoothAdapter = getBluetoothManager()?.adapter
            }
        }
        return mBluetoothAdapter!!
    }


    /**
     * Method Name : startScanDevices
     * Description : This method is used to start bluetooth(ble) device discovery
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun startScanDevices() {
        val METHOD_TAG = "[ startScanDevices ]"
        if (isBlueToothSupported()) {
            if (getBluetoothAdapter() != null) {
                if (isBluetoothEnabled()) {
                    if (!isScanning) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                            // Device scan callback LOLLIPOP
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (getBluetoothAdapter()?.state == BluetoothAdapter.STATE_ON) {
                                    scanner = getBluetoothAdapter()?.bluetoothLeScanner
                                    scanner?.startScan(
                                        scanFilterList,
                                        scanSetting,
                                        generateScanCallback()
                                    )
                                    Log.i(
                                        TAG,
                                        "BLE SCANNING STARTED"
                                    )

                                    isScanning = true
                                }

                            }, 3000)

                        } else {
                            getBluetoothAdapter()?.startLeScan(mLeScanCallback)

                            Log.i(
                                TAG,
                                "BLE SCANNING STARTED"
                            )

                            isScanning = true


                        }
                    } else {
                        Log.i(
                            TAG,
                            "BLE SCANNING ALREADY STARTED",

                            )
                    }
                } else {
                    Log.w(TAG, "$METHOD_TAG  ERROR : Bluetooth is Off")
                }
            } else {

                Log.w(
                    TAG,
                    "Bluetooth Adapter not initialized"
                )

            }
        } else {
            Log.w(TAG, "$METHOD_TAG  ERROR : Bluetooth not supported")
        }
    }

    fun getScanRunningStatus(): Boolean {
        return isScanning
    }

    /**
     * Method Name : stopScanDevice
     * Description : This method is used to stop bluetooth(ble) device discovery
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun stopScanDevice() {
        METHOD_TAG = "[ ${BLEController::stopScanDevice.name} ]"
        if (isBlueToothSupported()) {
            isScanning = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                scanner = getBluetoothAdapter()?.bluetoothLeScanner
                scanner?.stopScan(generateScanCallback())
                scanCallback=null
                scanner=null
                false
            } else {
                getBluetoothAdapter()?.stopLeScan(mLeScanCallback)
                getBluetoothAdapter()?.cancelDiscovery()
                false
            }


            Log.d(
                TAG,
                "BLE SCANNING STOPPED",

                )

        }
    }

    /**
     * BluetoothAdapter.LeScanCallback is callback registered for listening the new device discovered while scanning.
     */
    private val mLeScanCallback =
        LeScanCallback { device, rssi, scanRecord ->
            if (ableToScanDevice(device)) {
                if (rssi >= -70) {


                    Log.d(
                        TAG,
                        "DISCOVERED DEVICE : [ ${device.name} - ${device.address} - $rssi - ${device.bondState}]",

                        )



                    if (device.name.contains("UW-302")) {
                        val scanRecordItem: ScanRecordParser.ScanRecordItem =
                            ScanRecordParser.getParser().parseString(scanRecord)
                        val manufacturerSpecificData: ByteArray =
                            scanRecordItem.getManufacturerSpecificData()
                        if (manufacturerSpecificData != null
                            && manufacturerSpecificData.size == 2
                        ) {

                        }
                    } else {

                    }
                }
            }
        }

    /**
     * Device scan callback Lollipop and above
     */
    private fun generateScanCallback(): ScanCallback? {
        if (scanCallback == null) {
            scanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    super.onScanResult(callbackType, result)
                    val device: BluetoothDevice = result.device
                    Log.d(
                        TAG,
                        "DISCOVERED DEVICE : [ ${device.name} - ${device.address} - ${result.rssi} - ${device.bondState}]",

                        )
                    if (ableToScanDevice(device)) {
                        if (result.rssi >= -70) {
//                            Log.d(
//                                TAG,
//                                "DISCOVERED DEVICE : [ ${device.name} - ${device.address} - ${result.rssi} - ${device.bondState}]",
//
//                                )


                            if (device.name.contains("UW-302")) {
                                val scanRecordItem: ScanRecordParser.ScanRecordItem =
                                    ScanRecordParser.getParser()
                                        .parseString(result.scanRecord!!.bytes)
                                val manufacturerSpecificData: ByteArray =
                                    scanRecordItem.getManufacturerSpecificData()
                                if (manufacturerSpecificData != null
                                    && manufacturerSpecificData.size == 2
                                ) {
                                }
                            } else {
                            }
                        }
                    }
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    super.onBatchScanResults(results)
                    Log.e(TAG, "[ onBatchScanResults ] results : $results")
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    Log.e(TAG, "[ onScanFailed ] errorCode : $errorCode")
                    if (errorCode.equals(1)) {
                        stopScanDevice()
                    }
                }
            }
        }
        return scanCallback
    }

    /**
     * Method Name : ableToScanDevice
     * Description: This method is used for filtering the device discovered result based on device name
     * and model number.
     *
     * @param device
     * @return true if device name/model falls in this category otherwise false
     */
    private fun ableToScanDevice(bluetoothDevice: BluetoothDevice?): Boolean {
        return if (bluetoothDevice == null) {
            false
        } else {
            var deviceName: String? = bluetoothDevice.name ?: return false
            var device = filterDeviceModelNumberOrNameList.find { capturedDeviceName ->
                deviceName!!.startsWith(
                    capturedDeviceName
                ) || deviceName!!.contains(capturedDeviceName)
            }
            if (device != null) {
                return true
            }
            false
        }
    }


    /**
     * Method Name : setScanSetting
     * Description : This method is used for setting UUID filter and
     * lower power consumption mode while scanning
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setScanSetting() {
        scanSetting = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

//        filterUUIDList.forEach { uuid: UUID ->
//            // create data uuid starting with the value 1
//            val data = ParcelUuid.fromString(uuid.toString())
//
//            // create a mask to ensure only that value is searched
//            val mask = ParcelUuid.fromString("1000FFFF-0000-0000-0000-000000000000")
//
//            // build the filter
//            val builder = ScanFilter.Builder()
//            builder.setServiceUuid(data, mask)
//            val filter = builder.build()
//
//
//            // add the filter to the scanner
//            scanFilterList.clear()
//            scanFilterList.add(filter)
//        }
        // create data uuid starting with the value 1
        val data = ParcelUuid.fromString("0000fff0-b5ce-4e99-a40f-4b1e122d00d0")

        // create a mask to ensure only that value is searched
        val mask = ParcelUuid.fromString("1000FFFF-0000-0000-0000-000000000000")

        // build the filter
        val builder = ScanFilter.Builder()
//            builder.setServiceUuid(data, mask)
        val filter = builder.build()


        // add the filter to the scanner
        scanFilterList = ArrayList<ScanFilter>()
        scanFilterList.add(filter)

    }

    /**
     * Method Name : addNewUUIDFilter
     * Description : This method is used to add UUID to UUID Filter List
     * @param uuidString : string representing new UUID
     */
    fun addNewUUIDFilter(uuidString: String) {
        try {
            filterUUIDList.plus(UUID.fromString(uuidString))
        } catch (e: Exception) {
            Log.e(TAG, "[ addUUIDFilter ] ERROR : ${e.message}")
        }
    }

    /**
     * Method Name : addAllNewUUIDFilter
     * Description : This method is used to add UUID to UUID Filter List
     * @param uuidStringList : string representing new UUID
     */
    fun addAllNewUUIDFilter(uuidStringList: List<UUID>) {
        try {
            filterUUIDList.clear()
            filterUUIDList.addAll(uuidStringList)
        } catch (e: Exception) {
            Log.e(TAG, "[ addAllNewUUIDFilter ] ERROR : ${e.message}")
        }
    }

    /**
     * Method Name : addNewDeviceNameOrModelFilter
     * Description : This method is used to add device name/model no to name/model filter list
     * @param deviceInfoString : string representing new device name/model
     */
    fun addNewDeviceNameOrModelFilter(deviceInfoString: String) {
        try {
            filterDeviceModelNumberOrNameList.plus(deviceInfoString)
        } catch (e: Exception) {
            Log.e(TAG, "[ addNewDeviceNameOrModelFilter ] ERROR : ${e.message}")
        }
    }

    /**
     * Method Name : addNewDeviceNameOrModelFilterList
     * Description : This method is used to add device name/model no to name/model filter list
     * @param deviceInfoList : string representing new device name/model
     */
    fun addNewDeviceNameOrModelFilterList(deviceInfoList: List<String>) {
        try {
            filterDeviceModelNumberOrNameList.clear()
            filterDeviceModelNumberOrNameList.addAll(deviceInfoList)
        } catch (e: Exception) {
            Log.e(TAG, "[ addNewDeviceNameOrModelFilterList ] ERROR : ${e.message}")
        }
    }


}



