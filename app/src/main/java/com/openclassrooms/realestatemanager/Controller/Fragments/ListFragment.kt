package com.openclassrooms.realestatemanager.Controller.Fragments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.internal.BottomNavigationItemView
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.openclassrooms.realestatemanager.Controller.Activities.AddActivity
import com.openclassrooms.realestatemanager.Controller.Activities.MainActivity
import com.openclassrooms.realestatemanager.Controller.ViewModel.EstateViewModel
import com.openclassrooms.realestatemanager.Controller.Views.FragmentListAdapter
import com.openclassrooms.realestatemanager.Di.Injection
import com.openclassrooms.realestatemanager.Models.Estate
import com.openclassrooms.realestatemanager.Models.FullEstate

import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils.Constants
import com.openclassrooms.realestatemanager.Utils.DividerItemDecoration
import com.openclassrooms.realestatemanager.Utils.ItemClickSupport
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_list.*

/**
 * A simple [Fragment] subclass.
 *
 */

const val DATABASE_ID = "DATABASE_ID"

class ListFragment : Fragment() {

    private lateinit var listEstate:ArrayList<FullEstate>
    private lateinit var adapter:FragmentListAdapter
    private lateinit var mViewModel: EstateViewModel

    companion object {
        fun newInstance(): ListFragment {
            return ListFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.e("ListFragment", "Displaying fragment...")
        mViewModel = ViewModelProviders.of(this,Injection.provideViewModelFactory(this.context!!)).get(EstateViewModel::class.java)

        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = arguments?.size()
        if (bundle != null) {
            val query = arguments?.getString("QUERY")
            val args = arguments?.getStringArrayList("ARGS") as ArrayList<Any>
            mViewModel.getEstatesBySearch(query!!,args).observe(this, Observer { updateUI(it!!) })
        }else{
            mViewModel.getEstates().observe(this, Observer<List<FullEstate>> { updateUI(it!!) })
        }

        this.configureRecyclerView()
        this.configureOnClickRecyclerView()
    }

    // ---------------------
    // CONFIGURATION
    // ---------------------
    private fun configureRecyclerView(){
        this.listEstate = ArrayList()
        this.adapter = FragmentListAdapter(this.listEstate)
        this.fragment_list_recycler_view.adapter = this.adapter
        this.fragment_list_recycler_view.layoutManager = LinearLayoutManager(activity)
        fragment_list_recycler_view.addItemDecoration(DividerItemDecoration(activity as Activity,0, 0))
    }

    private fun configureOnClickRecyclerView(){
        ItemClickSupport.addTo(fragment_list_recycler_view, R.layout.fragment_list_item)
                .setOnItemClickListener{recyclerView, position, v ->
                    if ((activity as MainActivity).isTablet()){
                        adapter.setBackgroundColorItemClicked(position)
                    }
                    this.launchDetailFragment(adapter.getEstateInfos(position).estate.id)
        }
    }

    // ---------------------
    // ACTION
    // ---------------------

    private fun launchDetailFragment(databaseId : Long){
        val bundle = Bundle()
        val newFragment = DetailFragment.newInstance()
        bundle.putLong(DATABASE_ID,databaseId)
        newFragment.arguments = bundle

        val transaction = activity!!.supportFragmentManager.beginTransaction()

        if((activity as MainActivity).isTablet()){
            transaction.replace(R.id.fragment_view_detail, newFragment)
        }else{
            transaction.replace(R.id.fragment_view, newFragment)
        }

        transaction.addToBackStack(null)
        transaction.commit()

    }

    // ---------------------
    // UI
    // ---------------------

    private fun updateUI(results:List<FullEstate>){
        //results.forEach{ Log.e("LIST_UPDATE_UI","Estate Type/ID : ${it.estate.estateType}"+ " / ${it.estate.id} : " + it.images.toString())}
        this.listEstate.clear()
        this.listEstate.addAll(results)
        adapter.notifyDataSetChanged()
    }

}
