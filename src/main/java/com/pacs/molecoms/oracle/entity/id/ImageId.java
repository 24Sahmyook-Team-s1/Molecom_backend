package com.pacs.molecoms.oracle.entity.id;

import lombok.*;
import java.io.Serializable;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class ImageId implements Serializable {
    private Long studyKey;
    private Long seriesKey;
    private Long imageKey;
}
