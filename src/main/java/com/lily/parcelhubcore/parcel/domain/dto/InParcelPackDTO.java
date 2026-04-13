package com.lily.parcelhubcore.parcel.domain.dto;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelDO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelOpRecordDO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.WaybillRegistryDO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InParcelPackDTO {

    private WaybillRegistryDO waybillRegistryDO;

    private ParcelDO insertParcelDO;

    private ParcelDO updateParcelDO;

    private ParcelOpRecordDO parcelOpRecordDO;

}
