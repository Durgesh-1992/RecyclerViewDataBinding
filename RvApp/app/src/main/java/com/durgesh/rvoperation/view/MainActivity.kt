package com.durgesh.rvoperation.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.durgesh.rvoperation.R
import com.durgesh.rvoperation.adapter.EmployeeDetailAdapter
import com.durgesh.rvoperation.databinding.MainUi
import com.durgesh.rvoperation.listener.ItemClickListener
import com.durgesh.rvoperation.listener.ItemOperation
import com.durgesh.rvoperation.model.Employee
import com.durgesh.rvoperation.model.EmployeeDiffCallback
import com.durgesh.rvoperation.viewModel.EmployeeDetailViewModel

class MainActivity : AppCompatActivity(), ItemClickListener, ItemOperation {
    private var mRecyclerView: RecyclerView? = null
    private var mRecyclerViewAdapter: EmployeeDetailAdapter? = null
    private var mEmployeeDetailViewModel: EmployeeDetailViewModel? = null
    private var activityMainBinding: MainUi? = null
    private val TAG = "MainActivity"
    private var mList = ArrayList<Employee>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        activityMainBinding?.handlerItem = this
        mEmployeeDetailViewModel =
            ViewModelProviders.of(this@MainActivity).get(EmployeeDetailViewModel::class.java)

        mRecyclerViewAdapter = EmployeeDetailAdapter(
            mList, mEmployeeDetailViewModel!!, this@MainActivity
        )
        mRecyclerView =
            activityMainBinding?.recyclerView//findViewById<RecyclerView>(R.id.recycler_view)
        mRecyclerView?.layoutManager = LinearLayoutManager(this)
        mRecyclerView?.adapter = mRecyclerViewAdapter
        mEmployeeDetailViewModel?.getEmployeeList()
            ?.observe(this, Observer { it ->
                it.forEach { employee ->
                    Log.i(
                        TAG,
                        "ITEM : NAME : ${employee.name} :: ID : ${employee.id} :: PERFORMANCE : ${employee.performance}"
                    )
                    val isPresent = mList.find { it.id.equals(employee.id) }
                    if (isPresent == null) {
                        mList.add(employee)
                    }
                }
                val newList = mList.sortedBy { employee -> employee.id }
                    mRecyclerViewAdapter?.updateEmployeeListItems(newList)


            })
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
}