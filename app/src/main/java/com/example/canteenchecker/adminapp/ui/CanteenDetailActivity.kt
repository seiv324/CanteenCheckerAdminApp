package com.example.canteenchecker.adminapp.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.*
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.example.canteenchecker.adminapp.core.CanteenUpdateParameters
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class CanteenDetailActivity : AppCompatActivity() {
    companion object {
        private const val DEFAULT_ZOOM_FACTOR = 15f
        fun intent(context: Context) =
            Intent(context, CanteenDetailActivity::class.java)
    }

    // Create receiver since it is a abstract class
    private val receiver = object: CanteenChangedBroadcastReceiver() {
        override fun onReceiveCanteenChanged(canteenId: String) {
            if(currentCanteenId == canteenId ){
                loadCanteenDetails()
            }
        }
    }

    // not possible in companion object
    private lateinit var authenticationToken : String
    private lateinit var currentCanteenId : String

    private lateinit var txvName: TextView
    private lateinit var edtName: EditText
    private lateinit var txvLocation: TextView
    private lateinit var edtLocation: EditText
    private lateinit var txvPhone: TextView
    private lateinit var edtPhone: EditText
    private lateinit var txvWebsite: TextView
    private lateinit var edtWebsite: EditText
    private lateinit var txvWaitingTime: TextView
    //private lateinit var edtName: EditText
    private lateinit var txvDish: TextView
    private lateinit var edtDishName: EditText
    private lateinit var txvDishPrice: TextView

    // Buttons
    private lateinit var btnUpdateCanteen : Button
    private lateinit var btnUpdateDish : Button
    private lateinit var btnUpdateWaitingTime : Button
    private lateinit var btnShowReviews : Button

    // Map Fragmente
    private lateinit var mapFragment: SupportMapFragment
    // Necessary geocode listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canteen_detail)

        authenticationToken = (application as CanteenCheckerAdminApplication).authenticationToken ?: ""
        currentCanteenId = ""

        txvName = findViewById(R.id.canteenDetailNameTxtField)
        edtName = findViewById(R.id.edtDetailName)
        txvLocation = findViewById(R.id.canteenDetailAddressTxtField)
        edtLocation = findViewById(R.id.edtDetailAddress)
        txvPhone = findViewById(R.id.canteenDetailPhoneNumberTxtField)
        edtPhone = findViewById(R.id.edtDetailPhoneNumber)
        txvWebsite = findViewById(R.id.canteenDetailWebsiteTxtField)
        edtWebsite = findViewById(R.id.edtDetailWebsite)
        txvWaitingTime = findViewById(R.id.canteenDetailWaitingTimeTxtField)

        txvDish = findViewById(R.id.canteenDetailDishTxtField)
        edtDishName = findViewById(R.id.edtDetailDish)
        txvDishPrice = findViewById(R.id.canteenDetailDishPriceTxtField)

        btnUpdateCanteen = findViewById(R.id.updateCanteenButton)
        btnUpdateCanteen.setOnClickListener{ updateCanteenDetails() }

        btnUpdateDish = findViewById(R.id.updateDishButton)
        btnUpdateDish.setOnClickListener{ updateDishDetails() }

        btnShowReviews = findViewById(R.id.showReviews)
        btnShowReviews.setOnClickListener{ startActivity(ReviewsActivity.intent(this)) }

        btnUpdateWaitingTime = findViewById(R.id.updateWaitingTimeButton)
        btnUpdateWaitingTime.setOnClickListener{ updateWaitingTime() }
        // Add listener for Firebase
        registerCanteenChangedBroadcastReceiver(receiver)

        /* val geocodeListener = @RequiresApi(33) object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                // do something with the addresses list
                edtLocation.setText(addresses[0].getAddressLine(0).toString())
            }
        }*/

        //maps
        mapFragment = supportFragmentManager.findFragmentByTag(getString(R.string.tag_map_fragment)) as SupportMapFragment
        mapFragment.getMapAsync{
            val apply = it.uiSettings.apply {
                setAllGesturesEnabled(true)
                isZoomControlsEnabled = true
            }
            it.setOnMarkerDragListener(object: GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(p0: Marker) {
                    // Nothing
                }

                override fun onMarkerDrag(p0: Marker) {
                    // Nothing
                }

                //@RequiresApi(Build.VERSION_CODES.TIRAMISU)
                override fun onMarkerDragEnd(currentMarker: Marker) {
                    lifecycleScope.launch {
                        var newPosition = Geocoder(this@CanteenDetailActivity)
                            .getFromLocation(currentMarker.position.latitude, currentMarker.position.longitude, 1) //, geocodeListener);
                            ?.get(0)?.getAddressLine(0)?.toString()

                        edtLocation.setText(newPosition)
                    }
                }
            })
        }

        loadCanteenDetails()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterCanteenChangedBroadcastReceiver(receiver)
    }

    private fun loadCanteenDetails() = lifecycleScope.launch{
        if(authenticationToken != null){
            AdminApiFactory.createAdminApiInstance().getCanteen(authenticationToken)
                .onFailure {
                    Toast.makeText(this@CanteenDetailActivity, R.string.message_canteen_not_found, Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                .onSuccess { currentCanteen ->
                    //txvName.text = currentCanteen.name
                    //txvLocation.text = currentCanteen.location
                    //txvPhone.text = currentCanteen.phoneNumber
                    //txvWebsite.text = currentCanteen.website
                    //txvWaitingTime.text = currentCanteen.waitingTime.toString()
                    //txvDish.text = currentCanteen.dish
                    //txvDishPrice.text = currentCanteen.dishPrice.toString()

                    if(currentCanteenId == ""){
                        currentCanteenId = currentCanteen.id
                        (application as CanteenCheckerAdminApplication).canteenId = currentCanteenId
                    }

                    edtName.setText(currentCanteen.name)
                    edtLocation.setText(currentCanteen.location)
                    edtPhone.setText(currentCanteen.phoneNumber)
                    edtWebsite.setText(currentCanteen.website)
                    edtDishName.setText(currentCanteen.dish)
                    txvDishPrice.text = currentCanteen.dishPrice.toString()
                    txvWaitingTime.text = currentCanteen.waitingTime.toString()

                    lifecycleScope.launch{
                        val address = Geocoder(this@CanteenDetailActivity)
                            .getFromLocationName(currentCanteen.location, 1)
                            ?.firstOrNull()?.run{ LatLng(latitude, longitude) }

                        mapFragment.getMapAsync{ map ->
                            map.apply {
                                clear()
                                //show address
                                if(address != null){
                                    addMarker(MarkerOptions().position(address).draggable(true))
                                    animateCamera(CameraUpdateFactory.newLatLngZoom(address, DEFAULT_ZOOM_FACTOR))
                                } else {
                                    animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0,0.0), 0f))
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun updateCanteenDetails() = lifecycleScope.launch{
        var canteenToUpdate = CanteenUpdateParameters(
            edtName.text.toString(),
            edtLocation.text.toString(),
            edtWebsite.text.toString(),
            edtPhone.text.toString()
        )

        if(authenticationToken != null) {
            AdminApiFactory.createAdminApiInstance()
                .updateCanteenData(authenticationToken, canteenToUpdate)
                .onFailure {
                    Toast.makeText(
                        this@CanteenDetailActivity,
                        R.string.message_update_canteen_failure,
                        Toast.LENGTH_LONG
                    ).show()
                }
                .onSuccess {
                    Toast.makeText(
                        this@CanteenDetailActivity,
                        R.string.message_update_canteen_success,
                        Toast.LENGTH_LONG
                    ).show()
                    loadCanteenDetails()
                }
        }
    }

    private fun updateDishDetails() {
        val view = layoutInflater.inflate(R.layout.dialog_update_dish, null)
        // Set values
        view.findViewById<EditText>(R.id.dishUpdateNameField).setText(txvDish.text.toString())
        view.findViewById<com.google.android.material.slider.RangeSlider>(R.id.rangeSliderUpdateDishPrice).setValues(txvDishPrice.text.toString().toFloat())

        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_update_dish_title)
            .setView(view)
            .setPositiveButton(R.string.dialog_update_dish_positive_button){ dialog, _ ->
                dialog.dismiss()
                lifecycleScope.launch{
                    AdminApiFactory.createAdminApiInstance().updateDish(
                        authenticationToken,
                        view.findViewById<EditText>(R.id.dishUpdateNameField).text.toString(),
                        view.findViewById<com.google.android.material.slider.RangeSlider>(R.id.rangeSliderUpdateDishPrice).values.last().toDouble()
                    )
                    .onFailure {
                        Toast.makeText(this@CanteenDetailActivity, R.string.dish_update_failure, Toast.LENGTH_SHORT).show()
                    }
                    .onSuccess {
                        Toast.makeText(this@CanteenDetailActivity, R.string.dish_update_success, Toast.LENGTH_SHORT).show()
                    }
                }
            }.create().show()
    }

    private fun updateWaitingTime() {
        val view = layoutInflater.inflate(R.layout.dialog_update_waitingtime, null)
        // Set Values
        view.findViewById<com.google.android.material.slider.RangeSlider>(R.id.rangeSliderUpdateWaitingTime).setValues(txvWaitingTime.text.toString().toFloat())
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_update_waiting_time_title)
            .setView(view)
            .setPositiveButton(R.string.dialog_update_waitingtime_positive_button)
            { dialog, _ ->
                dialog.dismiss()
                lifecycleScope.launch{
                    AdminApiFactory.createAdminApiInstance().updateWaitingTime(
                        authenticationToken,
                        view.findViewById<com.google.android.material.slider.RangeSlider>(R.id.rangeSliderUpdateWaitingTime).values.last().toInt()
                    )
                    .onFailure {
                        Toast.makeText(this@CanteenDetailActivity, R.string.waitingtime_update_failed, Toast.LENGTH_SHORT).show()
                    }
                    .onSuccess {
                        Toast.makeText(this@CanteenDetailActivity, R.string.waitingtime_update_sucess, Toast.LENGTH_SHORT).show()
                    }
                }
            }.create().show()
    }

}


