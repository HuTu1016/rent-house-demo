package com.renthouse.operation.service;

import com.renthouse.operation.PropertyController.Building;
import com.renthouse.operation.PropertyController.Unit;

public interface PropertyService {
    String createBuilding(Building r);
    String createUnit(Unit r);
}
