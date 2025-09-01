// com/pacs/molecoms/oracle/dto/ImageDto.java
package com.pacs.molecoms.dicomfile.dto;

import com.pacs.molecoms.oracle.entity.ImageEntity;

public record ImageDto(
        Long studyKey,
        Long seriesKey,
        Long imageKey,
        String sopInstanceUid,
        String sopClassUid,
        String path,
        String fname
) {
    public static ImageDto from(ImageEntity e) {
        return new ImageDto(
                e.getStudyKey(),
                e.getSeriesKey(),
                e.getImageKey(),
                e.getSopInstanceUid(),
                e.getSopClassUid(),
                e.getPath(),
                e.getFname()
        );
    }
}
