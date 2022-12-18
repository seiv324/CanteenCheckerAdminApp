package com.example.canteenchecker.adminapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import kotlinx.coroutines.launch

class CanteenDetailActivity : AppCompatActivity() {
    // not possible in companion object
    private lateinit var authenticationToken : String

    private lateinit var txvName: TextView
    private lateinit var txvLocation: TextView
    private lateinit var txvPhone: TextView
    private lateinit var txvWebsite: TextView
    private lateinit var txvWaitingTime: TextView
    private lateinit var txvDish: TextView
    private lateinit var txvDishPrice: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canteen_detail)

        authenticationToken = (application as CanteenCheckerAdminApplication).authenticationToken!!

        txvName = findViewById(R.id.canteenDetailNameTxtField)
        txvLocation = findViewById(R.id.canteenDetailAddressTxtField)
        txvPhone = findViewById(R.id.canteenDetailPhoneNumberTxtField)
        txvWebsite = findViewById(R.id.canteenDetailWebsiteTxtField)
        txvWaitingTime = findViewById(R.id.canteenDetailWaitingTimeTxtField)

        txvDish = findViewById(R.id.canteenDetailDishTxtField)
        txvDishPrice = findViewById(R.id.canteenDetailDishPriceTxtField)

        updateCanteenDetails()
    }

    private fun updateCanteenDetails() = lifecycleScope.launch{
        if(authenticationToken != null){
            AdminApiFactory.createAdminApiInstance().getCanteen(authenticationToken!!)
                .onFailure {
                    Toast.makeText(
                        this@CanteenDetailActivity,
                        R.string.message_canteen_not_found,
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                .onSuccess { currentCanteen ->
                    txvName.text = currentCanteen.name
                    txvLocation.text = currentCanteen.location
                    txvPhone.text = currentCanteen.phoneNumber
                    txvWebsite.text = currentCanteen.website
                    txvWaitingTime.text = currentCanteen.waitingTime.toString()
                    txvDish.text = currentCanteen.dish
                    txvDishPrice.text = currentCanteen.dishPrice.toString()
                }
        }

    }
}