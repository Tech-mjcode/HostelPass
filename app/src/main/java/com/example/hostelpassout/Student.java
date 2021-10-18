package com.example.hostelpassout;

import org.parceler.Parcel;

public class Student {
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private String roomNo;
    private String profilePic;
    private String course;
    private String forHostel;
    private String userId;


    public Student(String name, String email, String phoneNumber, String password, String roomNo, String profilePic, String course, String forHostel, String userId) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.roomNo = roomNo;
        this.profilePic = profilePic;
        this.course = course;
        this.forHostel = forHostel;
        this.userId = userId;
    }



    public Student() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getForHostel() {
        return forHostel;
    }

    public void setForHostel(String forHostel) {
        this.forHostel = forHostel;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
