package com.catprogrammer.myfrigo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.model.GeneralCallback
import com.catprogrammer.myfrigo.util.HttpUtil
import com.catprogrammer.myfrigo.util.ListViewAdapter
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {

    private var foods = ArrayList<Food>()
    private lateinit var mAdapter: ListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // add swipe down refresh listener, call getData() when swipe down
        foodlist_swiperefresh.setOnRefreshListener { getData() }

        // add adapter to listview, link dataToShow to adapter
        mAdapter = ListViewAdapter(this, emptyList())
        foodlist_listview.adapter = mAdapter
        foodlist_listview.setOnItemClickListener { _: AdapterView<*>, _: View, i: Int, _: Long ->
            if(mAdapter.isClosed(i)) {
                val food = foods[i]
                val intent = Intent(this, FoodDetailActivity::class.java)
                intent.putExtra("food", food)
                startActivity(intent)
            } // else ignore event
        }

        // btn to camera
        btn_to_camera.setOnClickListener {
            mAdapter.closeAllItems()
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        // buttons sort
        btn_sort_expiration.setOnClickListener {
            mAdapter.closeAllItems()
            mAdapter.updateDataSet(
                    foods.sortedWith(
                            getComparator(SortBy.EXP, SortDirection.ASC, SortBy.PRD,
                                    SortDirection.ASC, SortBy.UPL, SortDirection.DESC)))
        }
        btn_sort_production.setOnClickListener {
            mAdapter.closeAllItems()
            mAdapter.updateDataSet(
                    foods.sortedWith(
                            getComparator(SortBy.PRD, SortDirection.ASC, SortBy.EXP,
                                    SortDirection.ASC, SortBy.UPL, SortDirection.DESC)))
        }
        btn_sort_upload.setOnClickListener {
            mAdapter.closeAllItems()
            mAdapter.updateDataSet(
                    foods.sortedWith(
                            getComparator(SortBy.UPL, SortDirection.DESC, SortBy.PRD,
                                    SortDirection.ASC, SortBy.EXP, SortDirection.ASC)))
        }
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    /**
     * Get all foods data, and update the list view
     */
    private fun getData() {
        val cb: GeneralCallback = object : GeneralCallback() {
            override fun onSuccess(data: JsonObject) {
                val entries = data.entrySet() // foods

                foods = ArrayList()
                for (map in entries) { // one food
                    val foodJson = map.value.asJsonObject
                    foods.add(Food.populateFood(foodJson))
                }
                foods.sortWith(
                        getComparator(SortBy.EXP, SortDirection.ASC, SortBy.PRD,
                                SortDirection.ASC, SortBy.UPL, SortDirection.DESC))

                runOnUiThread {
                    mAdapter.updateDataSet(foods)
                    foodlist_swiperefresh.isRefreshing = false
                }
            }

            override fun onFailure() {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Request failure", Toast.LENGTH_LONG).show()
                }
            }
        }// val cb end

        foodlist_swiperefresh.isRefreshing = true
        HttpUtil.getInstance().getAllFoods(cb)
    }

    /**
     * Get a Food comparator for sorting the Food list
     *
     * @property    sortBy1 1st attribute to sort
     * @property    sortBy1 2st attribute to sort
     * @property    sortBy1 3st attribute to sort
     * @property    sortDirection1 sort direction of sortBy1
     * @property    sortDirection2 sort direction of sortBy2
     * @property    sortDirection3 sort direction of sortBy3
     */
    private fun getComparator(sortBy1: SortBy, sortDirection1: SortDirection,
                              sortBy2: SortBy, sortDirection2: SortDirection,
                              sortBy3: SortBy, sortDirection3: SortDirection): Comparator<Food> {

        fun internalGetComparator(sb: SortBy, sd: SortDirection, comparator: Comparator<Food>?): Comparator<Food> {
            return when (sb) {
                SortBy.PRD -> when (sd) {
                    SortDirection.ASC ->
                        comparator?.thenBy(nullsLast(), Food::productionDate)
                                ?: compareBy(nullsLast(), Food::productionDate)

                    SortDirection.DESC ->
                        comparator?.thenByDescending(nullsLast(), Food::productionDate)
                                ?: compareByDescending(nullsLast(), Food::productionDate)
                }
                SortBy.EXP -> when (sd) {
                    SortDirection.ASC ->
                        comparator?.thenBy(nullsLast(), Food::expirationDate)
                                ?: compareBy(nullsLast(), Food::expirationDate)

                    SortDirection.DESC ->
                        comparator?.thenByDescending(nullsLast(), Food::expirationDate)
                                ?: compareByDescending(nullsLast(), Food::expirationDate)
                }
                SortBy.UPL -> when (sd) {
                    SortDirection.ASC ->
                        comparator?.thenBy(nullsLast(), Food::createdAt)
                                ?: compareBy(nullsLast(), Food::createdAt)

                    SortDirection.DESC ->
                        comparator?.thenByDescending(nullsLast(), Food::createdAt)
                                ?: compareByDescending(nullsLast(), Food::createdAt)
                }
            }
        }

        var comparator = internalGetComparator(sortBy1, sortDirection1, null)
        comparator = internalGetComparator(sortBy2, sortDirection2, comparator)
        comparator = internalGetComparator(sortBy3, sortDirection3, comparator)
        return comparator

    }

    private enum class SortDirection {
        DESC, ASC
    }

    private enum class SortBy {
        PRD, EXP, UPL
    }
}


