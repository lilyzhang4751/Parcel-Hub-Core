package com.lily.parcelhubcore.user.api.response;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StationRegisterResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 5327169230211761984L;

    private String stationCode;
}
