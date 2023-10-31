package com.example.bluetoothexample

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.example.bluetoothexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var btPermission=false
    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)  // Converts activity_main.xml into binding, helps access different parts
        setContentView(binding.root) // Show this
        title="MetroLinx"
        val scaleUp = AnimationUtils.loadAnimation(this,R.anim.scale_up)
        val scaleDown = AnimationUtils.loadAnimation(this,R.anim.scale_down)
        scaleUp.duration = 1000
        binding.imageView2.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.imageView2.startAnimation(scaleDown)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    binding.imageView2.startAnimation(scaleUp)
                    binding.imageView2.postDelayed({
                        scanBt()
                    },scaleUp.duration)
                    true
                }
                else -> false
            }

        }
    }
    fun scanBt(){
    //    Timer().schedule(1000);

        val bluetoothManager: BluetoothManager =getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if(bluetoothAdapter==null){
            Toast.makeText(this,"No Device found",Toast.LENGTH_LONG).show()
            // Bluetooth not supported on device
        }
        else{
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){ // S part means Android 12, current version should be higher or equal
                bluetoothPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT)
                // Permission Launcher to connect to Bluetooth Devices
            }
            else{
                bluetoothPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_ADMIN)
                //
            }
        }
    }
    private val bluetoothPermissionLauncher =registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
            isGranted:Boolean-> // If user grants Bluetooth related permission
        if(isGranted){
            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
            btPermission=true // Permission Granted
            // Check if bluetooth is enabled
            if(bluetoothAdapter?.isEnabled==false){
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)// Request to enable Bluetooth through system settings
                btActivityResultLauncher.launch(enableBtIntent)
            }
            else{
                scanBT()
            }
        }
    }
    private val btActivityResultLauncher= registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
            result: ActivityResult ->
        if(result.resultCode == RESULT_OK){
            scanBT()
        }
    }
    @SuppressLint("MissingPermission") // Ignore missing permission warnings
    private fun scanBT() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java) // Provides access to BT functionalities
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        val builder = AlertDialog.Builder(this@MainActivity)
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.scan_bt,null)
        builder.setCancelable(false) // The user can't dismiss the dialog by tapping outside of it.
        builder.setView(dialogView)
        val btlst = dialogView.findViewById<ListView>(R.id.bt_lst)
        val dialog = builder.create()
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter?.bondedDevices as Set<BluetoothDevice>
        val ADAhere: SimpleAdapter
        var data: MutableList<Map<String?, Any?>?>? = null
        data = ArrayList()
        if (pairedDevices.isNotEmpty()) {
            val datanum1: MutableMap<String?, Any?> = HashMap()
            datanum1["A"] = ""
            datanum1["B"] = ""
            data.add(datanum1)
            for (device in pairedDevices) {
                val datanum: MutableMap<String?, Any?> =HashMap()
                datanum["A"] = device.name
                datanum["B"] = device.address
                data.add(datanum)
            }
            val fromwhere = arrayOf("A")
            val viewswhere = intArrayOf(R.id.item_name)
            ADAhere =
                SimpleAdapter(this@MainActivity, data, R.layout.item_list, fromwhere, viewswhere)
            btlst.adapter = ADAhere
            ADAhere.notifyDataSetChanged()
            btlst.onItemClickListener =
                AdapterView.OnItemClickListener { adapterView, view, position, l ->
                    val string = ADAhere.getItem(position) as HashMap<String,String>
                    val deviceName = string["A"]
                    binding.deviceName.text = deviceName
                    dialog.dismiss()
                }
        } else {
            val value = "No Devices found"
            Toast.makeText(this, value, Toast.LENGTH_LONG).show()
            return
        }
        dialog.show()
    }
}