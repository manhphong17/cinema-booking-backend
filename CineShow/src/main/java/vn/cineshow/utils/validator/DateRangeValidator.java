package vn.cineshow.utils.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.cineshow.dto.request.movie.MovieFilterRequest;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, MovieFilterRequest> {
    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(MovieFilterRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        var fromDate = request.getFromDate();
        var toDate = request.getToDate();
        if (fromDate == null || toDate == null) return true;
        return !fromDate.isAfter(toDate);
    }
}
