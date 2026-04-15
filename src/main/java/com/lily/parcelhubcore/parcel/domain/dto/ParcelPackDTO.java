package com.lily.parcelhubcore.parcel.domain.dto;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelDO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelOpRecordDO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.WaybillRegistryDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParcelPackDTO {

    private WaybillRegistryDO waybillRegistryDO;

    private ParcelDO parcelDO;

    private ParcelOpRecordDO parcelOpRecordDO;

}
