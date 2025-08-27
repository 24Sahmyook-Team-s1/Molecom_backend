// com/pacs/molecoms/oracle/dto/SeriesDto.java
package com.pacs.molecoms.dicomfile.dto;

import com.pacs.molecoms.oracle.entity.SeriesEntity;

public record SeriesDto(
        Long seriesKey,
        String seriesInsUid,
        String seriesDesc,
        String modality,
        String bodyPart,
        Long imageCnt
) {
    public static SeriesDto from(SeriesEntity e) {
        return new SeriesDto(
                e.getSeriesKey(),
                e.getSeriesInsUid(),
                e.getSeriesDesc(),
                e.getModality(),
                e.getBodyPart(),
                e.getImageCnt()
        );
    }
}
