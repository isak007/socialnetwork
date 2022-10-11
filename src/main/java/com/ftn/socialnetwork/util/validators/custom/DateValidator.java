package com.ftn.socialnetwork.util.validators.custom;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;

class DateValidator implements ConstraintValidator<Date, String> {

    @Override
    public boolean isValid(String sDate, ConstraintValidatorContext context) {
        try {
            java.util.Date date = new SimpleDateFormat("yyyy-MM-dd").parse(sDate);
            java.util.Date maxDate = new java.util.Date();
            java.util.Date minDate = new java.util.Date();
            maxDate.setYear(maxDate.getYear()-18);
            minDate.setYear(minDate.getYear()-120);
            // if date is before max allowed date return true, otherwise false
            return date.before(maxDate) && date.after(minDate);

        } catch (ParseException e) {
            return false;
        }

    }
}