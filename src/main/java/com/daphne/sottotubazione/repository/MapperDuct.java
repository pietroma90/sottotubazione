package com.geowebframework.sottotubazione.repository;

import com.geowebframework.sottotubazione.RowUpdateData;
import com.geowebframework.sottotubazione.domain.DuctTube;
import com.geowebframework.sottotubazione.domain.UndergroundRoute;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MapperDuct {

    @Select("select t.pk_lines_products                                          as id, " +
            "       t.fk_lines_trenches, " +
            "       t.fk_mat_duct, " +
            "       null                                                         as exsternal_diameter, " +
            "       coalesce(t.fk_parent_new_duct, t.fk_parent_exi_duct)         as parent_id, " +
            "       true                                                         as is_new, " +
            "       (SELECT count(*) " +
            "        FROM r_lines_products occ " +
            "        WHERE occ.fk_parent_new_duct = t.pk_lines_products)         as childCount, " +
            "       t.fk_parent_exi_duct IS NOT NULL or t.fk_parent_new_duct IS NOT NULL as is_child," +
            "md.short_descript::text as short_desc_name " +
            "from r_lines_products t " +
            "join mat_duct md on t.fk_mat_duct = md.pk_mat_duct " +
            "where t.drawing =  #{drawing} " +
            "union all " +
            "select t.pk_tubi_esistenti                                         as id, " +
            "       t.fk_lines_trenches, " +
            "       null                                                        as fk_mat_duct, " +
            "       t.exsternal_diameter, " +
            "       t.fk_parent_exi_duct                                        as parent_id, " +
            "       false                                                       as is_new, " +
            "       ( " +
            "           SELECT COUNT(*) " +
            "           FROM r_lines_products occ " +
            "           WHERE occ.fk_parent_exi_duct = t.pk_tubi_esistenti " +
            "       ) " +
            "           + " +
            "       ( " +
            "           SELECT COUNT(*) " +
            "           FROM tubi_esistenti occ " +
            "           WHERE occ.fk_parent_exi_duct = t.pk_tubi_esistenti " +
            "       )        as childCount, " +
            "       t.fk_parent_exi_duct IS NOT NULL as is_child," +
            "t.exsternal_diameter::text as short_desc_name " +
            "from tubi_esistenti t " +
            "where t.drawing = #{drawing}")
    List<DuctTube> findNuoviNonOccupatiByTratta(@Param("drawing") Long drawing);

    @Update("<script>" +
            "<foreach collection='rowUpdateDataList' item='rowUpdate' separator=';'>" +
            "UPDATE ${tableName} " +
            "SET " +
            "<foreach collection='rowUpdate.updateValues' index='key' item='val' separator=', '>" +
            "${key} = #{val} " +
            "</foreach>" +
            " WHERE " +
            "<foreach collection='rowUpdate.filterValues' index='key' item='val' separator=' AND '>" +
            "${key} = #{val}" +
            "</foreach>" +
            "</foreach>" +
            "</script>")
    void massiveUpdateEntityValuesByFilterValuesBatch(
            @Param("tableName") String tableName,
            @Param("rowUpdateDataList") List<RowUpdateData> rowUpdateDataList
    );

    @Select("select t.pk_prj_lines_trenches , t.name, t.trenches_types " +
            "from prj_lines_trenches t " +
            "join lines_types lt on lt.pk_lines_types =  t.trenches_types " +
            "join procedure_config.r_config_fk_parent_duct r on r.fk_lines_types_ids @> array[lt.pk_lines_types]" +
            "where drawing = #{drawing}")
    List<UndergroundRoute> retrieveUndergroundRoutesByDrawing(@Param("drawing") Long drawing);
}
