package com.jccv.tuprivadaapp.util;

import com.jccv.tuprivadaapp.exception.BadRequestException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Utilidad centralizada para validar y normalizar la información de contacto.
 */
public final class ContactInfoValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?\\d{7,15}$");
    private static final Pattern COUNTRY_CODE_PATTERN = Pattern.compile("^\\+\\d{1,4}$");

    private ContactInfoValidator() {
    }

    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String sanitized = phone.trim();
        if (sanitized.isEmpty()) {
            return null;
        }
        if (!PHONE_PATTERN.matcher(sanitized).matches()) {
            throw new BadRequestException("El teléfono debe contener entre 7 y 15 dígitos y puede iniciar con +");
        }
        return sanitized;
    }

    public static String normalizeCountryCode(String countryCode) {
        if (countryCode == null) {
            return null;
        }
        String sanitized = countryCode.trim();
        if (sanitized.isEmpty()) {
            return null;
        }
        if (!COUNTRY_CODE_PATTERN.matcher(sanitized).matches()) {
            throw new BadRequestException("El código de país debe iniciar con + y contener de 1 a 4 dígitos");
        }
        return sanitized;
    }
}
