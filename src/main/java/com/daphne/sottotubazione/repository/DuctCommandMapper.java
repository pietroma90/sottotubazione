package com.geowebframework.sottotubazione.repository;

import com.geowebframework.sottotubazione.RowUpdateData;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * CQRS - lato Command: solo operazioni di scrittura sui dati dei tubi.
 * Separato da DuctQueryMapper per rispettare il principio di segregazione delle interfacce.
 *
 * NOTA SICUREZZA: ${tableName} e ${key} sono vulnerabili a SQL injection.
 * I valori devono essere validati tramite TableNameValidator prima di chiamare questo mapper.
 */
public interface DuctCommandMapper {

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
    void batchUpdateByTable(
            @Param("tableName") String tableName,
            @Param("rowUpdateDataList") List<RowUpdateData> rowUpdateDataList
    );
}
