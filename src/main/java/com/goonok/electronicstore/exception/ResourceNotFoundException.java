package com.goonok.electronicstore.exception; // Or your preferred exception package

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Adding @ResponseStatus ensures Spring MVC returns a 404 Not Found status
// when this exception is uncaught by a controller.
@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Getter // Lombok annotation to generate getters
@Setter // Lombok annotation to generate setters (optional, usually just getters needed)
public class ResourceNotFoundException extends RuntimeException {

    private String resourceName;
    private String fieldName;
    private Object fieldValue; // Use Object to handle different types of IDs (Long, String, etc.)

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        // Construct the error message
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceNotFoundException(String userNotFound) {
        super(userNotFound);
    }

    // Overriding toString() can be helpful for logging
    @Override
    public String toString() {
        return String.format("%s{resourceName='%s', fieldName='%s', fieldValue=%s}",
                this.getClass().getSimpleName(), resourceName, fieldName, fieldValue);
    }
}