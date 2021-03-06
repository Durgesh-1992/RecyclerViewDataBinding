package com.durgesh.rvoperation.model;


import android.util.Log;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class EmployeeDiffCallback extends DiffUtil.Callback {

    private final List<Employee> mOldEmployeeList;
    private final List<Employee> mNewEmployeeList;
    private static final String TAG = "EmployeeDiffCallback";

    public EmployeeDiffCallback(List<Employee> oldEmployeeList, List<Employee> newEmployeeList) {
        this.mOldEmployeeList = oldEmployeeList;
        this.mNewEmployeeList = newEmployeeList;
    }

    @Override
    public int getOldListSize() {
        return mOldEmployeeList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewEmployeeList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return String.valueOf(mOldEmployeeList.get(oldItemPosition).getId()).equalsIgnoreCase(String.valueOf(mNewEmployeeList.get(
                newItemPosition).getId()));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Employee oldEmployee = mOldEmployeeList.get(oldItemPosition);
        final Employee newEmployee = mNewEmployeeList.get(newItemPosition);

        return (oldEmployee.getName().equals(newEmployee.getName())
                && oldEmployee.performance.equals(newEmployee.performance));
    }

    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        Log.d(TAG, "oldItemPosition " + oldItemPosition + " newItemPosition " + newItemPosition);
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
