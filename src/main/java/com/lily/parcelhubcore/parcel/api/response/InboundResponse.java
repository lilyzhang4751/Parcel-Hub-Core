package com.lily.parcelhubcore.parcel.api.response;

import java.io.Serial;
import java.io.Serializable;

import com.lily.parcelhubcore.shared.response.OpResultResponse;
import lombok.Data;

@Data
public class InboundResponse extends OpResultResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 130387519347434782L;


}
