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
@AllArgsConstructor
@NoArgsConstructor
public class ParcelDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 9212246964314765167L;

    private ParcelBaseInfoDTO baseInfo;

    private List<ParcelOpRecordDTO> opRecordList;

    private List<ParcelNotifyRecordDTO> notifyRecordList;
}
