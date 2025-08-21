package com.pacs.molecoms.oracle.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class OracleImageId implements Serializable {
    private Long studyKey;
    private Long seriesKey;
    private Long imageKey;
}
