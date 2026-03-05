package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.NeumaticoRequest;
import com.dliriotech.tms.tyreservice.dto.NeumaticoResponse;

import java.util.List;

public interface NeumaticoService {
    List<NeumaticoResponse> getAllNeumaticosByEquipoId(Integer equipoId, Integer empresaId);

    NeumaticoResponse saveNeumatico(NeumaticoRequest request, Integer empresaId);

    NeumaticoResponse updateNeumatico(Integer id, NeumaticoRequest request, Integer empresaId);
}