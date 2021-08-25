package com.durgesh.rvoperation.view

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.durgesh.rvoperation.R
import com.durgesh.rvoperation.adapter.EmployeeDetailAdapter
import com.durgesh.rvoperation.controller.BLEController
import com.durgesh.rvoperation.databinding.MainUi
import com.durgesh.rvoperation.listener.ItemClickListener
import com.durgesh.rvoperation.listener.ItemOperation
import com.durgesh.rvoperation.model.Employee
import com.durgesh.rvoperation.viewModel.EmployeeDetailViewModel


class MainActivity : AppCompatActivity(), ItemClickListener, ItemOperation {
    private var mRecyclerView: RecyclerView? = null
    private var mRecyclerViewAdapter: EmployeeDetailAdapter? = null
    private var mEmployeeDetailViewModel: EmployeeDetailViewModel? = null
    private var activityMainBinding: MainUi? = null
    private val TAG = "MainActivity"
    private var mList = ArrayList<Employee>()

    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    var mBLEController: BLEController? = null

    /**
     * BroadcastReceiver used for listen event related to bluetooth state ON/OFF
     */
    private var mBluetoothStateChangeReceiver: BroadcastReceiver? = null
    private var mBroadcastReceiverList = ArrayList<BroadcastReceiver>()

    /**
     * BroadcastReceiver for capturing action related new device found/discovered, data reading,failure and timeout
     */
    var gBroadcastReceiver: BroadcastReceiver? = null

    /**
     * BroadcastManager for handling and passing the action between android component
     */
    var gLocalBroadcastManager: LocalBroadcastManager? = null
    var mData_Error_BroadcastManager: LocalBroadcastManager? = null
    var mData_Error_BroadcastReceiver: BroadcastReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        activityMainBinding?.handlerItem = this
        mEmployeeDetailViewModel =
                ViewModelProviders.of(this@MainActivity).get(EmployeeDetailViewModel::class.java)
        activityMainBinding?.lifecycleOwner = this@MainActivity
        activityMainBinding?.viewModel = mEmployeeDetailViewModel
        mRecyclerViewAdapter = EmployeeDetailAdapter(
                mList, mEmployeeDetailViewModel!!, this@MainActivity
        )
        mRecyclerView =
                activityMainBinding?.recyclerView//findViewById<RecyclerView>(R.id.recycler_view)
        mRecyclerView?.layoutManager = LinearLayoutManager(this)
        mRecyclerView?.adapter = mRecyclerViewAdapter
        mEmployeeDetailViewModel?.getEmployeeList()
                ?.observe(this, Observer { it ->
//                it.forEach { employee ->
//                    Log.i(
//                        TAG,
//                        "ITEM : NAME : ${employee.name} :: ID : ${employee.id} :: PERFORMANCE : ${employee.performance}"
//                    )
//                    val isPresent = mList.find { it.id.equals(employee.id) }
//                    if (isPresent == null) {
//                        mList.add(employee)
//                    }
//                }
                    val newList = it.sortedBy { employee -> employee.id }
                    mRecyclerViewAdapter?.updateEmployeeListItems(newList)


                })
        registerBluetoothStateChangeListener()
    }


    override fun onItemClick(employee: Employee) {

    }

    override fun onIncrementClick(employee: Employee) {
        mEmployeeDetailViewModel?.incrementPerformance(employee)

    }

    override fun onDecrementClick(employee: Employee) {
        mEmployeeDetailViewModel?.decrementPerformance(employee)
    }


    override fun onItemAdd() {
        mEmployeeDetailViewModel?.addEmployee()
        Handler(Looper.getMainLooper()).postDelayed({
            mRecyclerView?.smoothScrollToPosition(mList.size - 1)
        }, 300)
    }

    override fun onItemDelete() {

    }

    override fun onItemAddAll() {
        mEmployeeDetailViewModel?.addAllEmployee()
        Handler(Looper.getMainLooper()).postDelayed({
            mRecyclerView?.smoothScrollToPosition(mList.size - 1)
        }, 300)
    }

    fun init() {
        if (mBLEController == null) {
            mBLEController = BLEController.getInstance(this)
        }
        mBLEController?.setScanSetting()
        mBLEController?.startScanDevices()
    }

    fun checkLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle("LOCATION ")
                        .setMessage("LOCATION PERMISSION REQUIRED")
                        .setPositiveButton("OK",
                                DialogInterface.OnClickListener { dialogInterface, i -> //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(
                                            this@MainActivity,
                                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                            MY_PERMISSIONS_REQUEST_LOCATION
                                    )
                                })
                        .create()
                        .show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            )
                            == PackageManager.PERMISSION_GRANTED
                    ) {

                        //Request location updates:
//                        locationManager.requestLocationUpdates(provider, 400, 1, this)
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
//        if (checkLocationPermission()) {
//            init()
//        }
    }

    override fun onPause() {
        super.onPause()
        mBLEController?.stopScanDevice()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBLEController = null
    }

    /**
     * Method Name : registerBluetoothStateChangeListener
     * Description : This method is used for registering the listener for capturing
     * the bluetooth state change event like BLUETOOTH_ON,BLUETOOTH_OFF
     */
    private fun registerBluetoothStateChangeListener() {
        val METHOD_TAG = "[ registerBluetoothStateChangeListener ]"

        if (mBluetoothStateChangeReceiver != null) {
            return
        }
        mBluetoothStateChangeReceiver = object : BroadcastReceiver() {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    when (intent.action) {
                        BluetoothAdapter.ACTION_STATE_CHANGED -> {
                            val state = intent.getIntExtra(
                                    BluetoothAdapter.EXTRA_STATE,
                                    BluetoothAdapter.ERROR
                            )
                            when (state) {
                                BluetoothAdapter.STATE_OFF -> {
                                    Log.i(
                                            TAG,
                                            "$METHOD_TAG BLUETOOTH STATE : BluetoothAdapter.STATE_OFF"
                                    )
                                    mBLEController?.stopScanDevice()
                                }
                                BluetoothAdapter.STATE_TURNING_OFF -> {

                                    Log.i(
                                            TAG,
                                            "$METHOD_TAG BLUETOOTH STATE : BluetoothAdapter.STATE_TURNING_OFF"
                                    )

                                }
                                BluetoothAdapter.STATE_ON -> {
                                    init()
                                    Log.i(
                                            TAG,
                                            "$METHOD_TAG BLUETOOTH STATE : BluetoothAdapter.STATE_ON"
                                    )


                                }
                                BluetoothAdapter.STATE_TURNING_ON -> {

                                    Log.i(
                                            TAG,
                                            "$METHOD_TAG BLUETOOTH STATE : BluetoothAdapter.STATE_TURNING_ON"
                                    )

                                }
                            }
                        }
                    }
                } else {
                    Log.i(
                            TAG,
                            "$METHOD_TAG ERROR : intent is null "
                    )
                }
            }

        }
        var intentFilter: IntentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        if (!mBroadcastReceiverList.contains(mBluetoothStateChangeReceiver!!)) {
            this?.applicationContext?.registerReceiver(
                    mBluetoothStateChangeReceiver,
                    intentFilter
            )
            mBroadcastReceiverList.add(mBluetoothStateChangeReceiver!!)
        }
    }

    /**
     * Method Name : unregisterReceiver
     * Description : This method is used for unregistering the listener registered for capturing event
     * related to library communication
     */
    fun unregisterReceiver() {
        gLocalBroadcastManager?.unregisterReceiver(
                gBroadcastReceiver!!
        )
        if (mBluetoothStateChangeReceiver != null) {
            if (mBroadcastReceiverList.contains(mBluetoothStateChangeReceiver!!)) {
                this?.applicationContext?.unregisterReceiver(mBluetoothStateChangeReceiver)
                mBroadcastReceiverList.remove(mBluetoothStateChangeReceiver!!)
            }
        }
    }

}