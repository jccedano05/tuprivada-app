package com.jccv.tuprivadaapp.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Servicio profesional para encriptación/desencriptación de datos sensibles.
 * Utiliza AES-256-GCM (Galois/Counter Mode) para autenticación y confidencialidad.
 * 
 * Casos de uso:
 * - API keys de pasarelas de pago
 * - Tokens de acceso
 * - Credenciales bancarias
 * - Información personal sensible
 */
@Service
@Slf4j
public class SensitiveDataEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    
    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public SensitiveDataEncryptionService(
            @Value("${app.security.encryption.key:changeme-this-is-not-secure-use-env-var}") String encryptionKey) {
        
        if ("changeme-this-is-not-secure-use-env-var".equals(encryptionKey)) {
            log.warn("⚠️ [Security] Usando clave de encriptación por defecto. CONFIGURE app.security.encryption.key en producción");
        }
        
        // Derivar clave AES-256 desde la contraseña configurada
        byte[] keyBytes = deriveKey(encryptionKey);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        this.secureRandom = new SecureRandom();
        
        log.info("[Security] Servicio de encriptación inicializado con AES-256-GCM");
    }

    /**
     * Encripta datos sensibles con AES-256-GCM.
     * 
     * @param plainText Texto plano a encriptar
     * @return Texto encriptado en Base64 (incluye IV + ciphertext + tag)
     * @throws RuntimeException si ocurre error en encriptación
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            // Generar IV aleatorio para cada encriptación
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // Concatenar IV + ciphertext (el tag GCM va incluido en cipherText)
            byte[] encryptedData = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encryptedData, 0, iv.length);
            System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);
            
            String encrypted = Base64.getEncoder().encodeToString(encryptedData);
            log.debug("[Security] Dato encriptado exitosamente (longitud: {})", encrypted.length());
            
            return encrypted;
            
        } catch (Exception e) {
            log.error("[Security] Error encriptando dato sensible: {}", e.getMessage());
            throw new RuntimeException("Error en encriptación de datos sensibles", e);
        }
    }

    /**
     * Desencripta datos previamente encriptados.
     * 
     * @param encryptedText Texto encriptado en Base64
     * @return Texto plano desencriptado
     * @throws RuntimeException si ocurre error en desencriptación
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            byte[] encryptedData = Base64.getDecoder().decode(encryptedText);
            
            // Extraer IV (primeros 12 bytes)
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);
            
            // Extraer ciphertext + tag
            byte[] cipherText = new byte[encryptedData.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedData, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            byte[] plainText = cipher.doFinal(cipherText);
            
            log.debug("[Security] Dato desencriptado exitosamente");
            
            return new String(plainText, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("[Security] Error desencriptando dato sensible: {}", e.getMessage());
            throw new RuntimeException("Error en desencriptación de datos sensibles", e);
        }
    }

    /**
     * Enmascara datos sensibles para logs (muestra solo primeros/últimos caracteres).
     * 
     * @param sensitiveData Dato sensible
     * @param visibleChars Caracteres visibles al inicio/fin
     * @return Dato enmascarado (ej: "key_***xyz")
     */
    public String maskForLogging(String sensitiveData, int visibleChars) {
        if (sensitiveData == null || sensitiveData.length() <= visibleChars * 2) {
            return "***";
        }
        
        String start = sensitiveData.substring(0, Math.min(visibleChars, sensitiveData.length()));
        String end = sensitiveData.substring(Math.max(0, sensitiveData.length() - visibleChars));
        
        return start + "***" + end;
    }

    /**
     * Deriva clave AES-256 desde contraseña usando hash SHA-256.
     * 
     * @param password Contraseña configurada
     * @return Bytes de clave AES-256
     */
    private byte[] deriveKey(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // AES-256 requiere 32 bytes
            return hash;
            
        } catch (Exception e) {
            throw new RuntimeException("Error derivando clave de encriptación", e);
        }
    }
    
    /**
     * Valida si un texto está encriptado (formato Base64 válido con longitud correcta).
     * 
     * @param text Texto a validar
     * @return true si parece estar encriptado
     */
    public boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        try {
            byte[] decoded = Base64.getDecoder().decode(text);
            // Debe tener al menos IV + algo de ciphertext + tag
            return decoded.length > GCM_IV_LENGTH + 16;
        } catch (Exception e) {
            return false;
        }
    }
}
