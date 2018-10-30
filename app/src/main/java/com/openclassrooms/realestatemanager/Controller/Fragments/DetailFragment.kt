package com.openclassrooms.realestatemanager.Controller.Fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.openclassrooms.realestatemanager.Controller.Activities.EditActivity
import com.openclassrooms.realestatemanager.Controller.Activities.MainActivity
import com.openclassrooms.realestatemanager.Controller.ViewModel.EstateViewModel
import com.openclassrooms.realestatemanager.Controller.Views.ActivityAddAdapter
import com.openclassrooms.realestatemanager.Di.Injection
import com.openclassrooms.realestatemanager.Models.FullEstate
import com.openclassrooms.realestatemanager.Models.Image

import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.R.id.detail_fragment_map
import com.openclassrooms.realestatemanager.Utils.Constants
import com.openclassrooms.realestatemanager.Utils.ItemClickSupport
import kotlinx.android.synthetic.main.fragment_detail.*

/**
 * A simple [Fragment] subclass.
 *
 */
class DetailFragment : Fragment(), ActivityAddAdapter.Listener, OnMapReadyCallback {

    private lateinit var mViewModel:EstateViewModel
    private var databaseId:Any? = null
    private lateinit var listImages:ArrayList<Image>
    private lateinit var adapter:ActivityAddAdapter
    private lateinit var mainDesc:String

    private lateinit var googleMap: GoogleMap
    private lateinit var mapView:MapView

    companion object {
        fun newInstance():DetailFragment{
            return DetailFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        mViewModel = ViewModelProviders.of(this, Injection.provideViewModelFactory(this.context!!)).get(EstateViewModel::class.java)

        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        detail_fragment_map.onCreate(savedInstanceState)
        detail_fragment_map.getMapAsync(this)

        this.retrieveDatabaseId()
        this.configureFABOnClickListener()
        this.configureRecyclerView()
        this.configureOnClickRecyclerView()
        this.configureRestoreDescButton()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.uiSettings.isCompassEnabled = false
        this.googleMap.uiSettings.isMyLocationButtonEnabled = false
        this.googleMap.uiSettings.isMapToolbarEnabled = false
        this.googleMap.uiSettings.setAllGesturesEnabled(false)
        this.googleMap.setOnMapClickListener {  }
    }

    // ---------------------
    // CONFIGURATION
    // ---------------------

    private fun configureRecyclerView(){
        this.listImages = ArrayList()
        this.adapter = ActivityAddAdapter(this.listImages,this, Constants.VIEW_HOLDER_ACTION_DETAIL)
        detail_fragment_recycler_view.adapter = this.adapter
        detail_fragment_recycler_view.layoutManager = LinearLayoutManager(this.context,LinearLayoutManager.HORIZONTAL,false)
    }

    private fun configureRestoreDescButton(){
        detail_fragment_restore_desc.setOnClickListener { detail_fragment_desc.text = mainDesc }
    }

    private fun configureOnClickRecyclerView(){
        ItemClickSupport.addTo(detail_fragment_recycler_view,R.layout.activity_add_item).setOnItemClickListener{
            recyclerView: RecyclerView?, position: Int, v: View? ->
                detail_fragment_desc.text = adapter.getImageDesc(position)
        }
    }

    private fun retrieveDatabaseId(){
        databaseId = arguments?.get(DATABASE_ID)
        Log.e("DETAIL_FRAGMENT","Id retrieved : $databaseId")
        if(databaseId != null){
            mViewModel.getEstatesByID(databaseId as Long).observe(this, Observer {
                updateUI(it!!)
            })
        }
    }

    private fun configureFABOnClickListener(){
        detail_fragment_fab.setOnClickListener {
            if (databaseId != null){
                val map = hashMapOf(DATABASE_ID to databaseId as Long)
                (activity as MainActivity).launchActivity(((activity as MainActivity).applicationContext),EditActivity::class.java,map)
            }
        }
    }

    // ---------------------
    // UI
    // ---------------------

    override fun onClickDeleteButton(position: Int) {
    }

    // ---------------------
    // UI
    // ---------------------

    private fun updateUI(result:FullEstate){
        listImages.clear()

        this.checkAndDisplayResultData(result)

        adapter.notifyDataSetChanged()
    }

    private fun checkAndDisplayResultData(result: FullEstate){
        if(result.estate.desc.isNullOrEmpty()){
            mainDesc = resources.getString(R.string.detail_fragment_no_desc)
            detail_fragment_desc.text = resources.getString(R.string.detail_fragment_no_desc)
        }else{
            mainDesc = result.estate.desc!!
            detail_fragment_desc.text = result.estate.desc
        }

        if (result.estate.surface.toString() == "null"){
            detail_fragment_surface.text = resources.getString(R.string.detail_fragment_not_specified)
        }else{
            detail_fragment_surface.text = result.estate.surface.toString()
        }

        if (result.estate.roomNumber.toString() == "null"){
            detail_fragment_rooms.text = resources.getString(R.string.detail_fragment_not_specified)
        }else{
            detail_fragment_rooms.text = result.estate.roomNumber.toString()
        }

        if (result.estate.bathroomNumber.toString() == "null"){
            detail_fragment_bathrooms.text = resources.getString(R.string.detail_fragment_not_specified)
        }else{
            detail_fragment_bathrooms.text = result.estate.bathroomNumber.toString()
        }

        if (result.estate.bedroomNumber.toString() == "null"){
            detail_fragment_bedrooms.text = resources.getString(R.string.detail_fragment_not_specified)
        }else{
            detail_fragment_bedrooms.text = result.estate.bedroomNumber.toString()
        }

        if (isAddressComplete(result)){
            detail_fragment_location_address.text = result.location.address
            detail_fragment_location_add_address.text = result.location.additionalAddress
            detail_fragment_location_city.text = result.location.city
            detail_fragment_location_zip.text = result.location.zipCode
            detail_fragment_location_country.text = result.location.country
            detail_fragment_map.visibility = View.VISIBLE
        }else{
            detail_fragment_location_address.text = resources.getString(R.string.detail_fragment_not_specified)
            detail_fragment_map.visibility = View.GONE
        }


        listImages.addAll(result.images)
    }

    private fun isAddressComplete(result: FullEstate) =
            !result.location.address.isNullOrEmpty() &&
                    !result.location.city.isNullOrEmpty() &&
                    !result.location.zipCode.isNullOrEmpty() &&
                    !result.location.country.isNullOrEmpty()
}
