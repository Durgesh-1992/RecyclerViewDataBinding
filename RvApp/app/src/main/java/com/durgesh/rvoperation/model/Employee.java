package com.durgesh.rvoperation.model;

public class Employee {

    public int id;
    public String name;
    public String role;
    public Integer performance = 0;

    public Employee(final int id, final String name, final String role, Integer performance) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.performance = performance;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public Integer getPerformance() {
        return performance;
    }

    public String getPerformanceString() {
        return String.valueOf(performance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Employee employee = (Employee) obj;

        if (id != employee.id) return false;
        if (!role.equalsIgnoreCase(employee.role)) return false;
        if (!getPerformanceString().equalsIgnoreCase(String.valueOf(employee.performance))) return false;
        return name != null ? name.equals(employee.name) : employee.name == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = result + (name != null ? name.hashCode() : 0);
        result = result + role.hashCode();
        return result;
    }
}
