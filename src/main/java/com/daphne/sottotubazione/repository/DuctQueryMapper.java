package com.geowebframework.sottotubazione.repository;

import com.geowebframework.sottotubazione.domain.DuctTube;
import com.geowebframework.sottotubazione.domain.UndergroundRoute;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * CQRS - lato Query: solo operazioni di lettura sui dati dei tubi.
 * Separato da DuctCommandMapper per rispettare il principio di segregazione delle interfacce.
 */
public interface DuctQueryMapper {

    @Select("select t.pk_lines_products                                          as id, " +
            "       t.fk_lines_trenches, " +
            "       t.fk_mat_duct, " +
            "       null                                                         as exsternal_diameter, " +
            "       coalesce(t.fk_parent_new_duct, t.fk_parent_exi_duct)         as parent_id, " +
            "       true                                                         as is_new, " +
            "       (SELECT count(*) " +
            "        FROM r_lines_products occ " +
            "        WHERE occ.fk_parent_new_duct = t.pk_lines_products)         as childCount, " +
            "       t.fk_parent_exi_duct IS NOT NULL or t.fk_parent_new_duct IS NOT NULL as is_child, " +
            "       md.short_descript::text as short_desc_name " +
            "from r_lines_products t " +
            "join mat_duct md on t.fk_mat_duct = md.pk_mat_duct " +
            "where t.drawing = #{drawing} " +
            "union all " +
            "select t.pk_tubi_esistenti                                          as id, " +
            "       t.fk_lines_trenches, " +
            "       null                                                         as fk_mat_duct, " +
            "       t.exsternal_diameter, " +
            "       t.fk_parent_exi_duct                                         as parent_id, " +
            "       false                                                        as is_new, " +
            "       (SELECT COUNT(*) FROM r_lines_products occ WHERE occ.fk_parent_exi_duct = t.pk_tubi_esistenti) " +
            "       + (SELECT COUNT(*) FROM tubi_esistenti occ WHERE occ.fk_parent_exi_duct = t.pk_tubi_esistenti) as childCount, " +
            "       t.fk_parent_exi_duct IS NOT NULL                             as is_child, " +
            "       t.exsternal_diameter::text                                   as short_desc_name " +
            "from tubi_esistenti t " +
            "where t.drawing = #{drawing}")
    List<DuctTube> findDuctsByDrawing(@Param("drawing") Long drawing);

    @Select("select t.pk_prj_lines_trenches, t.name, t.trenches_types " +
            "from prj_lines_trenches t " +
            "join lines_types lt on lt.pk_lines_types = t.trenches_types " +
            "join procedure_config.r_config_fk_parent_duct r on r.fk_lines_types_ids @> array[lt.pk_lines_types] " +
            "where drawing = #{drawing}")
    List<UndergroundRoute> findRoutesByDrawing(@Param("drawing") Long drawing);
}
