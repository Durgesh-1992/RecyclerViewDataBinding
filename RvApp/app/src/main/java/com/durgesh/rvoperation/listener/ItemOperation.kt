package com.durgesh.rvoperation.listener

import com.durgesh.rvoperation.model.Employee

interface ItemOperation {
    fun onItemAdd()
    fun onItemDelete()
    fun onItemAddAll()
}