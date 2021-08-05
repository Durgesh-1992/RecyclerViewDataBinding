package com.durgesh.rvoperation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.durgesh.rvoperation.R
import com.durgesh.rvoperation.databinding.ListItemBinding
import com.durgesh.rvoperation.listener.BindableAdapter
import com.durgesh.rvoperation.listener.ItemClickListener
import com.durgesh.rvoperation.model.Employee
import com.durgesh.rvoperation.model.EmployeeDiffCallback
import com.durgesh.rvoperation.viewModel.EmployeeDetailViewModel
import java.util.*
import kotlin.collections.ArrayList


class EmployeeDetailAdapter(
    private var employeeList: ArrayList<Employee>,
    private val mEmployeeDetailViewModel: EmployeeDetailViewModel,
    private val handler: ItemClickListener
) :/*BindableAdapter<ArrayList<Employee>>,*/
    RecyclerView.Adapter<EmployeeDetailAdapter.MyViewHolder>() {
    private val TAG = "EmployeeDetailAdapter"
    private var mEmployees = ArrayList<Employee>()

    init {
//        mEmployees.addAll(employeeList)
        this.mEmployees=employeeList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EmployeeDetailAdapter.MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.list_item, null, false)
        val binding = ListItemBinding.inflate(inflater)
        val lp = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.root.layoutParams = lp
        return MyViewHolder(binding, mEmployeeDetailViewModel, handler)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val employee = mEmployees[position]
        holder.bind(employee)
    }

    override fun getItemCount(): Int {
        return mEmployees.size
    }

    fun setData(newList: ArrayList<Employee>?) {
        this.mEmployees = ArrayList<Employee>(newList)
    }


    fun updateEmployeeListItems(employees: List<Employee?>?) {
        val mEmployeeList:ArrayList<Employee> = this.mEmployees.clone() as ArrayList<Employee>
        mEmployeeList.forEach { employee ->
            Log.i(
                TAG,
                "old mEmployees : NAME : ${employee.name} :: ID : ${employee.id} :: PERFORMANCE : ${employee.performance}"
            )
        }
        employees?.forEach { employee ->
            Log.i(
                TAG,
                "new employees : NAME : ${employee?.name} :: ID : ${employee?.id} :: PERFORMANCE : ${employee?.performance}"
            )
        }
        val diffCallback = EmployeeDiffCallback(mEmployeeList, employees)
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(diffCallback)
//        this.mEmployees.clear()
//        this.mEmployees.addAll(ArrayList<Employee>(employees))
        mEmployeeList.clear()
        mEmployeeList.addAll(ArrayList<Employee>(employees))
        setData(mEmployeeList)
        diffResult.dispatchUpdatesTo(this)

    }

    inner class MyViewHolder(
        private val listItemBinding: ListItemBinding,
        val mEmployeeDetailViewModel: EmployeeDetailViewModel,
        private val handler: ItemClickListener
    ) :
        RecyclerView.ViewHolder(listItemBinding.root) {

        fun bind(employee: Employee) {

            listItemBinding.mEmployee = employee
            listItemBinding.handler = handler

            listItemBinding.executePendingBindings()
        }
    }


//    override fun setData(data: ArrayList<Employee>) {
//
//    }
}