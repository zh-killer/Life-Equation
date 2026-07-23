package com.lifeequation.service;

import com.lifeequation.dto.DoctorWorkbenchDTO;

public interface DoctorWorkbenchService {
    DoctorWorkbenchDTO getWorkbenchOverview(Long doctorId);
}
