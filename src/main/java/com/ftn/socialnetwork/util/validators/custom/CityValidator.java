package com.ftn.socialnetwork.util.validators.custom;

import com.ftn.socialnetwork.util.RestService;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class CityValidator implements ConstraintValidator<City, String> {

    private final RestService restService;

    CityValidator(RestService restService) {
        this.restService = restService;
    }

    @Override
    public boolean isValid(String addressInput, ConstraintValidatorContext context) {
        String cityInput = addressInput.split(",")[0];
        ResponseEntity<Object> response = restService.getCityList(cityInput);
        if (response.getBody() == null) {return false;}
        Object responseBody = response.getBody();

        Gson gson = new Gson();
        try {
            String responseBodyString = gson.toJson(responseBody);
            responseBodyString = responseBodyString.substring(11);
            responseBodyString = responseBodyString.split("}", 2)[0];
            responseBodyString = responseBodyString.split("\\{", 2)[1];
            responseBodyString = "{" + responseBodyString + "}";

            JSONObject jsonObject = new JSONObject(responseBodyString);
            String city = jsonObject.get("city").toString();
            String countryName = jsonObject.get("countryName").toString();
            String countryCode = jsonObject.get("countryCode").toString();
            try {
                // if postal code exists
                String postalCode = jsonObject.get("postalCode").toString();
                return addressInput.equals(postalCode + " " + city + ", " + countryName + " " +countryCode);
            } catch(JSONException e){
                // else
                return addressInput.equals(city + ", " + countryName + " " +countryCode);
            }

        } catch(Exception e){
            return false;
        }

    }
}