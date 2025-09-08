package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

/**
 * Excepción lanzada cuando el RTD actual calculado es mayor al RTD actual existente en la base de datos.
 * Esta validación es crucial para mantener la integridad de los datos de desgaste de neumáticos,
 * ya que el RTD (profundidad de rodadura) solo puede disminuir con el uso, nunca aumentar.
 */
public class RtdInvalidIncrementException extends TyreServiceException {
    
    private final Integer neumaticoId;
    private final BigDecimal rtdActualExistente;
    private final BigDecimal rtdActualCalculado;

    public RtdInvalidIncrementException(Integer neumaticoId, BigDecimal rtdActualExistente, BigDecimal rtdActualCalculado) {
        super(
            String.format("RTD inválido para neumático %d: el RTD actual calculado (%.2f) no puede ser mayor al RTD actual existente (%.2f). El RTD solo puede disminuir con el uso.", 
                neumaticoId, rtdActualCalculado, rtdActualExistente),
            HttpStatus.UNPROCESSABLE_ENTITY,
            "TYR-RTD-INCR-001"
        );
        this.neumaticoId = neumaticoId;
        this.rtdActualExistente = rtdActualExistente;
        this.rtdActualCalculado = rtdActualCalculado;
    }

    public Integer getNeumaticoId() {
        return neumaticoId;
    }

    public BigDecimal getRtdActualExistente() {
        return rtdActualExistente;
    }

    public BigDecimal getRtdActualCalculado() {
        return rtdActualCalculado;
    }
}