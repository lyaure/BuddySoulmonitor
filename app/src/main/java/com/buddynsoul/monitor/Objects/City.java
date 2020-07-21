package com.buddynsoul.monitor.Objects;

public class City {

    private String cityName, countryName, keyValue;

    public City(String cityName, String countryName, String keyValue){
        this.cityName = cityName;
        this.countryName = countryName;
        this.keyValue = keyValue;
    }

    public City(){
        cityName = "City not found";
        countryName = "";
        keyValue = "";

    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getKeyValue() {
        return keyValue;
    }
}


