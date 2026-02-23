package com.dliriotech.tms.tyreservice.exception;

import java.math.BigDecimal;

/**
 * Excepción lanzada cuando el RTD actual calculado es mayor al RTD actual existente en la base de datos.
 */
public class RtdInvalidIncrementException extends TyreServiceException {

    private final Integer neumaticoId;
    private final BigDecimal rtdActualExistente;
    private final BigDecimal rtdActualCalculado;

    public RtdInvalidIncrementException(Integer neumaticoId, BigDecimal rtdActualExistente, BigDecimal rtdActualCalculado) {
        super(ErrorCode.INSPECCION_RTD_INVALID,
            String.format("RTD inválido para neumático %d: el RTD actual calculado (%.2f) no puede ser mayor al RTD actual existente (%.2f). El RTD solo puede disminuir con el uso.",
                neumaticoId, rtdActualCalculado, rtdActualExistente));
        this.neumaticoId = neumaticoId;
        this.rtdActualExistente = rtdActualExistente;
        this.rtdActualCalculado = rtdActualCalculado;
    }

    public Integer getNeumaticoId() { return neumaticoId; }
    public BigDecimal getRtdActualExistente() { return rtdActualExistente; }
    public BigDecimal getRtdActualCalculado() { return rtdActualCalculado; }
}