package com.dune.monitorme;

public class Driver {
    private String name;
    private String contact;
    private String email;
    private String password;
    private String address;
    private String cLicenseno;
    private String imageurl;
    private String route;

    public Driver(){

    }

    Driver(String address, String cLicenseno, String contact, String email, String imageurl, String name, String password, String route) {
        this.name = name;
        this.contact = contact;
        this.email = email;
        this.password = password;
        this.address = address;
        this.cLicenseno = cLicenseno;
        this.imageurl = imageurl;
        this.route = route;
    }

    public Driver(String address, String clicenseno, String contact, String email, String name , String password) {
        this.name = name;
        this.contact = contact;
        this.email = email;
        this.password = password;
        this.address = address;
        this.cLicenseno = clicenseno;
    }

    public Driver(String address, String clicenoseno,String contact,String email,String imageurl, String name , String password ) {
        this.name = name;
        this.contact = contact;
        this.email = email;
        this.password = password;
        this.address = address;
        this.cLicenseno = clicenoseno;
        this.imageurl = imageurl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact)
    {
        this.contact = contact;
    }

    String getCLicenseno() {
        return cLicenseno;
    }

    public void setCLicenseno(String cLicenseno) {
        this.cLicenseno = cLicenseno;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }
}
