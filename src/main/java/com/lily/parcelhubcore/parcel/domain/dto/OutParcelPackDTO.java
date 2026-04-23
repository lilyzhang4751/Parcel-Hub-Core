package com.lily.parcelhubcore.parcel.domain.dto;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.Parcel;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelOpRecord;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.WaybillRegistry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutParcelPackDTO {

    private WaybillRegistry updateWaybillRegistry;

    private Parcel updateParcel;

    private ParcelOpRecord parcelOpRecord;
}
