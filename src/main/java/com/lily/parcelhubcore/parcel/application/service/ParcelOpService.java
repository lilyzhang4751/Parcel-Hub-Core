package com.lily.parcelhubcore.parcel.application.service;

import com.lily.parcelhubcore.parcel.application.command.ParcelInBoundCommand;
import com.lily.parcelhubcore.parcel.application.command.PrepareInCommand;
import com.lily.parcelhubcore.parcel.application.dto.PrepareInDTO;
import com.lily.parcelhubcore.shared.enums.OperateTypeEnum;

public interface ParcelOpService {

    public PrepareInDTO prepareIn(PrepareInCommand bo);

    public void inbound(ParcelInBoundCommand command);

    public void outBoundOrReturn(String waybillCode, OperateTypeEnum operateTypeEnum);

    public void transfer(String waybillCode, String shelfCode);

}
