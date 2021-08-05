package com.durgesh.rvoperation.listener

import com.durgesh.rvoperation.model.Employee

interface ItemClickListener {
    fun onItemClick(employee: Employee)
    fun onIncrementClick(employee: Employee)
    fun onDecrementClick(employee: Employee)
}