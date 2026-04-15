package com.geowebframework.underPiping.dao;

import com.geowebframework.underPiping.model.ConfigRule;
import com.geowebframework.underPiping.model.DuctTube;
import com.geowebframework.underPiping.model.UndergroundRoute;
import com.geowebframework.underPiping.mapper.MapperUnderPiping;
import it.eagleprojects.gisfocommons.dao.DaoCommonsAbstractClass;
import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class DaoUnderPiping extends DaoCommonsAbstractClass<DuctTube> {

    private final MapperUnderPiping mapperUnderPiping;

    @Override
    public String getTableName() {
        return "";
    }

    public List<ConfigRule> findActiveRules() {
        return mapperUnderPiping.findActiveRules();
    }

    public List<UndergroundRoute> retrieveUndergroundRoutesByDrawing(Long projectId){
        return mapperUnderPiping.retrieveUndergroundRoutesByDrawing(projectId);
    }

    public List<DuctTube> findNuoviNonOccupatiByTratta(Long projectId){
        return mapperUnderPiping.findNuoviNonOccupatiByTratta(projectId);
    }

    public void massiveUpdateEntityValuesByFilterValuesBatch(String s, List<RowUpdateData> rowUpdateData) {
        mapperUnderPiping.massiveUpdateEntityValuesByFilterValuesBatch(s, rowUpdateData);
    }
}