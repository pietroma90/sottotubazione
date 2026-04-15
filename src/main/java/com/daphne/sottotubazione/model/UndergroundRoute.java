package com.geowebframework.underPiping.model;

import com.geowebframework.webclient.model.serverDbEntity.prj.PrjLinesTrenches;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class UndergroundRoute extends PrjLinesTrenches {

    private Set<DuctTube> ductTubes = new HashSet<>();
}