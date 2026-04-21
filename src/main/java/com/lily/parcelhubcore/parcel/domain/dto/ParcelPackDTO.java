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
public class ParcelPackDTO {

    private String waybillCode;

    private WaybillRegistry waybillRegistry;

    private Parcel parcel;

    private ParcelOpRecord parcelOpRecord;

    private ParcelNotifyEvent parcelNotifyEvent;

    private ParcelOpSyncEvent parcelOpSyncEvent;

}
