package com.upload;

public class Student {
    private String rollNo;
    private String fullName;

    public Student() {
    }

    public Student(String rollNo, String fullName) {
        this.rollNo = rollNo;
        this.fullName = fullName;
    }

    public String getRollNo() {
        return rollNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
