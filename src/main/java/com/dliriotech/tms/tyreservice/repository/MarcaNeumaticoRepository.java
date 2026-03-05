package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.MarcaNeumatico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarcaNeumaticoRepository extends JpaRepository<MarcaNeumatico, Integer> {
}