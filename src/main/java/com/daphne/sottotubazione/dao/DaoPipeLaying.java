package com.geowebframework.pipeLaying.dao;

import com.geowebframework.pipeLaying.model.ConfigRule;
import com.geowebframework.pipeLaying.model.DuctTube;
import com.geowebframework.pipeLaying.model.UndergroundRoute;
import com.geowebframework.pipeLaying.mapper.MapperPipeLaying;
import it.eagleprojects.gisfocommons.utils.RowUpdateData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class DaoPipeLaying {

    private final MapperPipeLaying mapperPipeLaying;

    public List<ConfigRule> findActiveRules() {
        return mapperPipeLaying.findActiveRules();
    }

    public List<UndergroundRoute> retrieveUndergroundRoutesByDrawing(Long projectId){
        return mapperPipeLaying.retrieveUndergroundRoutesByDrawing(projectId);
    }

    public List<DuctTube> getDuctTubeByDrawing(Long projectId){
        return mapperPipeLaying.getDuctTubeByDrawing(projectId);
    }

    public void massiveUpdateEntityValuesByFilterValuesBatch(String s, List<RowUpdateData> rowUpdateData) {
        mapperPipeLaying.massiveUpdateEntityValuesByFilterValuesBatch(s, rowUpdateData);
    }
}