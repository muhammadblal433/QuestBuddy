package com.questbuddy.tripmember.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class UpdateStatusDTOTest {

    private Validator validator;

    @Before
    public void setup() {
        ValidatorFactory f = Validation.buildDefaultValidatorFactory();
        validator = f.getValidator();
    }

    @Test
    public void testValidAcceptedStatus() {
        UpdateStatusDTO dto = new UpdateStatusDTO("ACCEPTED");
        Set<ConstraintViolation<UpdateStatusDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidStatusRejected() {
        UpdateStatusDTO dto = new UpdateStatusDTO("DECLINED");
        Set<ConstraintViolation<UpdateStatusDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNullStatusRejected() {
        UpdateStatusDTO dto = new UpdateStatusDTO(null);
        Set<ConstraintViolation<UpdateStatusDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
