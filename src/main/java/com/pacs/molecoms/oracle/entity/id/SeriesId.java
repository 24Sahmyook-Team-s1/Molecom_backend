// com/pacs/molecoms/oracle/entity/id/SeriesId.java
package com.pacs.molecoms.oracle.entity.id;

import java.io.Serializable;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class SeriesId implements Serializable {
    private Long studyKey;
    private Long seriesKey;
}
