package com.lily.parcelhubcore.parcel.application.service;

import com.lily.parcelhubcore.parcel.application.command.ParcelInBoundCommand;
import com.lily.parcelhubcore.parcel.application.command.PrepareInCommand;
import com.lily.parcelhubcore.parcel.application.dto.prepareInDTO;

public interface ParcelOpService {

    public prepareInDTO prepareIn(PrepareInCommand bo);

    public void inbound(ParcelInBoundCommand command);

    public void outBound();

    public void returned();

    public void transfer();

    public void inventory();
}
