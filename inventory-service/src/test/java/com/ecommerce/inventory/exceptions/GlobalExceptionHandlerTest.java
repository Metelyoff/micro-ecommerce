package com.ecommerce.inventory.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationExceptions_shouldReturnBadRequestWithFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = List.of(
                new FieldError("object", "name", "Name is required"),
                new FieldError("object", "price", "Price must be positive")
        );

        when(bindingResult.getAllErrors()).thenReturn(List.copyOf(fieldErrors));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody())
                .containsEntry("name", "Name is required")
                .containsEntry("price", "Price must be positive");
    }

    @Test
    void handleItemBadRequestException_shouldReturnBadRequestStatus() {
        ItemBadRequestException ex = new ItemBadRequestException("Invalid input");

        ResponseEntity<String> response = handler.handleItemBadRequestException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Invalid input");
    }

    @Test
    void handleItemNotFoundException_shouldReturnNotFoundStatus() {
        ItemNotFoundException ex = new ItemNotFoundException("Item not found");

        ResponseEntity<String> response = handler.handleItemNotFoundException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isEqualTo("Item not found");
    }

}