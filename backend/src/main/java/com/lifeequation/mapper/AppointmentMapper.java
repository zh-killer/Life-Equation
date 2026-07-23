package com.lifeequation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeequation.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
}
