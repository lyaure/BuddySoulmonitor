package com.buddynsoul.monitor;

public class City {

    private String cityName, countryName, keyValue;

    public City(String cityName, String countryName, String keyValue){
        this.cityName = cityName;
        this.countryName = countryName;
        this.keyValue = keyValue;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }
}


