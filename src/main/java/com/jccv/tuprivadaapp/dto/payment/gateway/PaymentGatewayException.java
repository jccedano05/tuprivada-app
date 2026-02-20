package com.jccv.tuprivadaapp.dto.payment.gateway;

/**
 * Excepción personalizada para errores en pasarelas de pago
 */
public class PaymentGatewayException extends Exception {
    
    private String errorCode;
    private String gatewayErrorCode;
    private Integer httpStatus;
    
    public PaymentGatewayException(String message) {
        super(message);
    }
    
    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PaymentGatewayException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public PaymentGatewayException(String message, String errorCode, String gatewayErrorCode) {
        super(message);
        this.errorCode = errorCode;
        this.gatewayErrorCode = gatewayErrorCode;
    }
    
    public PaymentGatewayException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getGatewayErrorCode() {
        return gatewayErrorCode;
    }
    
    public void setGatewayErrorCode(String gatewayErrorCode) {
        this.gatewayErrorCode = gatewayErrorCode;
    }
    
    public Integer getHttpStatus() {
        return httpStatus;
    }
    
    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }
}
