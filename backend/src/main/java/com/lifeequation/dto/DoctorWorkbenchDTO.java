package com.lifeequation.dto;

import lombok.Data;
import java.util.List;

@Data
public class DoctorWorkbenchDTO {
    // 今日概览
    private int todayAppointmentCount;   // 今日预约人数
    private int completedConsultCount;   // 已完成问诊数
    private int pendingConsultCount;     // 待处理咨询数
    private Integer isAvailable;         // 医生当前接诊状态 1=接诊 0=停诊

    // 今日患者列表（按时段排列）
    private List<TodayPatient> todayPatients;

    @Data
    public static class TodayPatient {
        private Long appointmentId;
        private Long patientId;
        private String patientName;
        private String patientPhone;
        private String appointmentDate;
        private String timeSlot;
        private String symptomDesc;
        private String status;
    }
}
