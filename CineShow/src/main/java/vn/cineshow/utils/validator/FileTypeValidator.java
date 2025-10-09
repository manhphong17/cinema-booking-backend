package vn.cineshow.utils.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileTypeValidator implements ConstraintValidator<FileType, MultipartFile> {
    private List<String> allowedTypes;

    @Override
    public void initialize(FileType constraintAnnotation) {
        allowedTypes = Arrays.asList(constraintAnnotation.allowed());
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) return false;
        return allowedTypes.contains(file.getContentType());
    }
}
