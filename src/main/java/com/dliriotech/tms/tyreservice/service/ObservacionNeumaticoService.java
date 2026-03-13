package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoNuevoRequest;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoResponse;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoUpdateRequest;

import java.util.List;

public interface ObservacionNeumaticoService {
    List<ObservacionNeumaticoResponse> getAllObservacionesByNeumaticoIdAndTipoMovimientoId(Integer neumaticoId, Integer tipoMovimientoId, Integer empresaId);
    List<ObservacionNeumaticoResponse> getAllObservacionesByNeumaticoId(Integer neumaticoId, Integer empresaId);
    List<ObservacionNeumaticoResponse> getAllObservacionesPendientesAndByNeumaticoId(Integer neumaticoId, Integer empresaId);
    List<ObservacionNeumaticoResponse> getAllObservacionesPendientesAndByEquipoId(Integer equipoId, Integer empresaId);
    ObservacionNeumaticoResponse saveObservacion(ObservacionNeumaticoNuevoRequest request);
    ObservacionNeumaticoResponse updateObservacion(Integer id, ObservacionNeumaticoUpdateRequest request, Integer empresaId);
}