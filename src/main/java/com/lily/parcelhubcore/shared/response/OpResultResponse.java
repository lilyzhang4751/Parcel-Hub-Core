package com.lily.parcelhubcore.shared.response;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpResultResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -2014782340793616325L;

    private boolean result;
}
