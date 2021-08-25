package com.durgesh.rvoperation.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.durgesh.rvoperation.model.Employee

class EmployeeDetailViewModel : ViewModel() {
    private val TAG = "EmployeeDetailViewModel"
    private var mEmployeeLiveData: MutableLiveData<MutableList<Employee>>? = null
    private var mEmployeeList: MutableList<Employee> = ArrayList<Employee>()
    private var employeeCount = 9


    fun getEmployeeList(): LiveData<MutableList<Employee>> {
        if (mEmployeeLiveData == null) {
            mEmployeeLiveData = MutableLiveData()
            loadEmployee()
        }

        return mEmployeeLiveData!!
    }

    private fun loadEmployee() {
        mEmployeeList.add(Employee(1, "Employee 1", "Developer", 4))
        mEmployeeList.add(Employee(2, "Employee 2", "Tester", 5))
        mEmployeeList.add(Employee(3, "Employee 3", "Support", 4))
        mEmployeeList.add(Employee(4, "Employee 4", "Sales Manager", 3))
        mEmployeeList.add(Employee(5, "Employee 5", "Manager", 4))
        mEmployeeList.add(Employee(6, "Employee 6", "Team lead", 4))
        mEmployeeList.add(Employee(7, "Employee 7", "Scrum Master", 5))
        mEmployeeList.add(Employee(8, "Employee 8", "Sr. Tester", 6))
        mEmployeeList.add(Employee(9, "Employee 9", "Sr. Developer", 7))
        mEmployeeLiveData?.value = mEmployeeList
    }

     fun addEmployee() {
        employeeCount += 1
        mEmployeeList.add(Employee(employeeCount, "Employee $employeeCount", "Developer", 4))

        mEmployeeLiveData?.value = mEmployeeList
    }

     fun addAllEmployee() {
        for (i in employeeCount..employeeCount + 10)
            mEmployeeList.add(Employee(i + 1, "Employee ${i + 1}", "Developer", 4))
        employeeCount += 10
        mEmployeeLiveData?.value = mEmployeeList
    }

    fun incrementPerformance(employee: Employee) {
        val found = mEmployeeList.find { it.id == employee.id }
        Log.i(TAG, " found?.performance :: ${found?.performance} , ${found?.name}")
        found?.performance = found?.performance?.toInt()?.plus(1)
        Log.i(TAG, "updated found?.performance :: ${found?.performance} , ${found?.name}")
        if (mEmployeeList.indexOf(employee) < mEmployeeList.size && mEmployeeList.indexOf(employee) > -1) {
            mEmployeeList[mEmployeeList.indexOf(employee)] = found!!
//            mEmployeeLiveData?.value = mEmployeeList
        }
    }

    fun decrementPerformance(employee: Employee) {
        val found = mEmployeeList.find { it.id == employee.id }
        Log.i(TAG, "found?.performance :: ${found?.performance} , ${found?.name}")
        found?.performance = found?.performance?.toInt()?.minus(1)
        Log.i(TAG, "updated found?.performance :: ${found?.performance} , ${found?.name}")
        if (mEmployeeList.indexOf(employee) < mEmployeeList.size && mEmployeeList.indexOf(employee) > -1) {
            mEmployeeList[mEmployeeList.indexOf(employee)] = found!!
//            mEmployeeLiveData?.value = mEmployeeList
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}