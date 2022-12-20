package com.example.canteenchecker.adminapp.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.canteenchecker.adminapp.*
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.example.canteenchecker.adminapp.core.ReviewEntry
import com.example.canteenchecker.adminapp.utils.SwipeToDeleteCallback
import kotlinx.coroutines.launch

class ReviewsActivity : AppCompatActivity() {
    // Create receiver since it is a abstract class
    private val receiver = object: CanteenChangedBroadcastReceiver() {
        override fun onReceiveCanteenChanged(canteenId: String) {
            if(currentCanteenId == canteenId ){
                updateReviews()
            }
        }
    }

    private val reviewsAdapter = ReviewsAdapter()

    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var rcvReviews: RecyclerView

    private lateinit var authenticationToken : String
    private lateinit var currentCanteenId : String


    companion object{
        fun intent(context: Context) : Intent =
            Intent(context, ReviewsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)

        authenticationToken = (application as CanteenCheckerAdminApplication).authenticationToken ?: ""
        currentCanteenId = ""

        swipeLayout = findViewById(R.id.srlSwipeRefreshLayout)
        rcvReviews = findViewById(R.id.rcvReviews)
        // Bind recycler view on adapter
        rcvReviews.adapter = reviewsAdapter
        swipeLayout.setOnRefreshListener { updateReviews() }

        // Bind Swipe handling to the adapter
        val swipeHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rcvReviews.adapter as ReviewsAdapter
                val position = viewHolder.adapterPosition

                // Make API call
                var review = adapter.getReviewFromPosition(position)
                deleteReview(review.id, position, adapter)
            }
        }
        // Touch Helper which is responsible for handling the swipe motion
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(rcvReviews)

        // Register for Reviews Updates
        registerCanteenChangedBroadcastReceiver(receiver)

        updateReviews()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterCanteenChangedBroadcastReceiver(receiver)
    }

    private fun updateReviews() = lifecycleScope.launch {
        swipeLayout.isRefreshing = true
        reviewsAdapter.displayReviews(
            AdminApiFactory.createAdminApiInstance().getReviewsForCanteen(authenticationToken).getOrElse {
                Toast.makeText(this@ReviewsActivity, R.string.message_loading_reviews_failed, Toast.LENGTH_LONG).show()
                emptyList()
            }
        )
        swipeLayout.isRefreshing = false
    }

    private fun deleteReview(reviewId : String, position : Int, tmpAdapter : ReviewsAdapter) = lifecycleScope.launch {
        AdminApiFactory.createAdminApiInstance().deleteReview(authenticationToken, reviewId)
            .onSuccess {
                Toast.makeText(this@ReviewsActivity, R.string.message_delete_review_success, Toast.LENGTH_LONG).show()
                tmpAdapter.notifyItemRemoved(position)
                // Remove from array
                updateReviews()
            }
            .onFailure {
                Toast.makeText(this@ReviewsActivity, R.string.message_delete_review_failed, Toast.LENGTH_LONG).show()
            }
    }

    // Inner Class
    private class ReviewsAdapter : RecyclerView.Adapter<ReviewsAdapter.ViewHolder>(){
        private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txvCreator: TextView = itemView.findViewById(R.id.txvCreator)
            val txvRemark: TextView = itemView.findViewById(R.id.txvRemark)
            val txvCreationDate: TextView = itemView.findViewById(R.id.txvCreationDate)
            val rtbRating: RatingBar = itemView.findViewById(R.id.rtbRating)
        }

        private var reviewEntries = emptyList<ReviewEntry>()

        // Inflate Layout for single entry
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_reviews, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.run {
            val review = reviewEntries[position]
            txvCreator.text = review.creator
            txvRemark.text = review.remark
            txvCreationDate.text = review.creationDate.toString()
            rtbRating.rating = review.rating
        }

        override fun getItemCount(): Int = reviewEntries.size

        fun displayReviews(reviews: List<ReviewEntry>){
            this.reviewEntries = reviews
            notifyDataSetChanged()
        }

        fun getReviewFromPosition(position: Int) : ReviewEntry = reviewEntries[position]

    }
}