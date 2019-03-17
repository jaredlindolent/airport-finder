package com.jared.airportfinder.Classes;

public class Flights {
    String type;
    String status;
    String departTime;
    String flightNum;
    String flightName;

    public Flights(String type, String status, String departTime, String flightName, String flightNum){
        this.type = type;
        this.status = status;
        this.departTime = departTime;
        this.flightNum = flightNum;
        this.flightName = flightName;
    }

    public String getType() { return type; }

    public String getStatus() {
        return status;
    }

    public String getDepartTime() { return departTime; }

    public String getFlightNum() {
        return flightNum;
    }

    public String getFlightName() {
        return flightName;
    }
}
