package com.geowebframework.sottotubazione.repository;

import com.geowebframework.sottotubazione.domain.ConfigRule;
import it.eagleprojects.gisfocommons.typehandler.BigIntArrayTypeHandler;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MapperConfigRule {

    @Select("select * from procedure_config.r_config_fk_parent_duct where not is_deleted order by priority_rules_order")
    @Result(column = "fk_lines_types_ids", property = "fk_lines_types_ids" , typeHandler = BigIntArrayTypeHandler.class)
    List<ConfigRule> findActiveOrderedByPriority();


}
