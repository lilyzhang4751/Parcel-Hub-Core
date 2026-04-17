package com.lily.parcelhubcore.parcel.api.response;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParcelQueryResultResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -3114385389998204887L;

    private int total;

    private List<ParcelInfoDTO> parcelList;
}
