package com.geowebframework.underPiping.domain;

import com.geowebframework.webclient.model.serverDbEntity.prj.PrjLinesTrenches;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

/**
 * Mappa la tabella prj_lines_trenches.
 * Rappresenta una tratta interrata (scavo nuovo o esistente).
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UndergroundRoute extends PrjLinesTrenches {

    private Set<DuctTube> ductTubes = new HashSet<>();
}
