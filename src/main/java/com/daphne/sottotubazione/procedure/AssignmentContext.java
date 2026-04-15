package com.geowebframework.underPiping.procedure;

import com.geowebframework.underPiping.model.UndergroundRoute;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignmentContext {
    private UndergroundRoute tratta;
}
