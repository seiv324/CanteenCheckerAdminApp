package com.example.canteenchecker.adminapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.example.canteenchecker.adminapp.core.Canteen
import com.example.canteenchecker.adminapp.core.CanteenDetails
import com.example.canteenchecker.adminapp.core.CanteenUpdateParameters
import kotlinx.coroutines.launch

class CanteenDetailActivity : AppCompatActivity() {
    // not possible in companion object
    private lateinit var authenticationToken : String

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
    private lateinit var btnShowReviews : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canteen_detail)

        authenticationToken = (application as CanteenCheckerAdminApplication).authenticationToken!!

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

        loadCanteenDetails()

        btnUpdateCanteen = findViewById(R.id.updateCanteenButton)
        btnUpdateCanteen.setOnClickListener{ updateCanteenDetails() }

        btnShowReviews = findViewById(R.id.showReviews)
        btnShowReviews.setOnClickListener{ startActivity(ReviewsActivity.intent(this)) }

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

                    edtName.setText(currentCanteen.name)
                    edtLocation.setText(currentCanteen.location)
                    edtPhone.setText(currentCanteen.phoneNumber)
                    edtWebsite.setText(currentCanteen.website)
                    edtDishName.setText(currentCanteen.dish)
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
                    //loadCanteenDetails()
                }
        }
    }
}