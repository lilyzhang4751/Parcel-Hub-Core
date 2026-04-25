package com.lily.parcelhubcore.parcel.application.service;

import com.lily.parcelhubcore.parcel.application.command.ParcelInBoundCommand;
import com.lily.parcelhubcore.parcel.application.command.PrepareInCommand;
import com.lily.parcelhubcore.parcel.application.dto.PrepareInDTO;
import com.lily.parcelhubcore.shared.enums.OperateTypeEnum;

public interface ParcelOpService {

    PrepareInDTO prepareIn(PrepareInCommand bo);

    void inbound(ParcelInBoundCommand command);

    void outBoundOrReturn(String waybillCode, OperateTypeEnum operateTypeEnum);

    void transfer(String waybillCode, String shelfCode);

}
