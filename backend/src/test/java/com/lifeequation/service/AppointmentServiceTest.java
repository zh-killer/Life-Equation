package com.lifeequation.service;

import com.lifeequation.config.TestConfig;
import com.lifeequation.entity.Appointment;
import com.lifeequation.entity.Doctor;
import com.lifeequation.entity.Hospital;
import com.lifeequation.mapper.DoctorMapper;
import com.lifeequation.mapper.HospitalMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 预约挂号服务测试 — 验证创建预约、医生状态校验、用户预约列表查询。
 */
@SpringBootTest
@Import(TestConfig.class)
@DisplayName("预约挂号服务测试")
class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @MockBean
    private DoctorMapper doctorMapper;

    @MockBean
    private HospitalMapper hospitalMapper;

    private Doctor availableDoctor;
    private Doctor unavailableDoctor;

    @BeforeEach
    void setUp() {
        availableDoctor = new Doctor();
        availableDoctor.setId(1L);
        availableDoctor.setName("张医生");
        availableDoctor.setIsAvailable(1);

        unavailableDoctor = new Doctor();
        unavailableDoctor.setId(2L);
        unavailableDoctor.setName("李医生");
        unavailableDoctor.setIsAvailable(0);
    }

    // ==================== 创建预约 ====================

    @Test
    @DisplayName("创建预约 — 医生可接诊时应成功创建")
    void createAppointment_DoctorAvailable_ShouldSucceed() {
        when(doctorMapper.selectById(1L)).thenReturn(availableDoctor);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", 1);
        params.put("doctorId", 1L);
        params.put("hospitalId", 1L);
        params.put("appointmentDate", LocalDate.now().plusDays(1).toString());
        params.put("timeSlot", "09:00-10:00");
        params.put("symptomDesc", "头痛三天");

        Long id = appointmentService.createAppointment(params);

        assertNotNull(id);
        assertTrue(id > 0);
    }

    @Test
    @DisplayName("创建预约 — 医生不接诊时应抛出异常")
    void createAppointment_DoctorUnavailable_ShouldThrow() {
        when(doctorMapper.selectById(2L)).thenReturn(unavailableDoctor);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", 1);
        params.put("doctorId", 2L);
        params.put("hospitalId", 1L);
        params.put("appointmentDate", LocalDate.now().plusDays(1).toString());
        params.put("timeSlot", "10:00-11:00");
        params.put("symptomDesc", "常规检查");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> appointmentService.createAppointment(params));
        assertEquals("该医生暂不接诊", ex.getMessage());
    }

    @Test
    @DisplayName("创建预约 — 医生不存在时应抛出异常")
    void createAppointment_DoctorNotFound_ShouldThrow() {
        when(doctorMapper.selectById(anyLong())).thenReturn(null);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", 1);
        params.put("doctorId", 999L);
        params.put("hospitalId", 1L);
        params.put("appointmentDate", LocalDate.now().plusDays(1).toString());
        params.put("timeSlot", "14:00-15:00");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> appointmentService.createAppointment(params));
        assertEquals("该医生暂不接诊", ex.getMessage());
    }

    // ==================== 查询预约 ====================

    @Test
    @DisplayName("查询预约 — 无预约记录返回空")
    void getUserAppointments_NoRecords_ShouldReturnEmpty() {
        var page = appointmentService.getUserAppointments(99999L, 1, 10);

        assertNotNull(page);
        assertEquals(0, page.getTotal());
    }

    @Test
    @DisplayName("查询预约 — 刚创建的预约包含正确的状态和日期")
    void getUserAppointments_NewAppointment_ShouldHaveCorrectStatus() {
        when(doctorMapper.selectById(1L)).thenReturn(availableDoctor);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", 1);
        params.put("doctorId", 1L);
        params.put("hospitalId", 1L);
        params.put("appointmentDate", LocalDate.now().plusDays(3).toString());
        params.put("timeSlot", "16:00-17:00");

        Long id = appointmentService.createAppointment(params);

        // 直接查询刚创建的预约
        Appointment appt = appointmentService.getById(id);
        assertNotNull(appt);
        assertEquals("PENDING", appt.getStatus());
        assertEquals(LocalDate.now().plusDays(3), appt.getAppointmentDate());
        assertEquals("16:00-17:00", appt.getTimeSlot());
    }

    // ==================== 管理端 ====================

    @Test
    @DisplayName("管理端查询 — 按状态筛选")
    void adminPage_FilterByStatus_ShouldWork() {
        // 全部状态（不筛选）
        var all = appointmentService.getAppointmentPageAdmin(1, 10, null);
        assertNotNull(all);

        // 按 PENDING 筛选
        var pending = appointmentService.getAppointmentPageAdmin(1, 10, "PENDING");
        assertNotNull(pending);
        pending.getRecords().forEach(a -> assertEquals("PENDING", a.getStatus()));

        // 按不存在的状态筛选
        var none = appointmentService.getAppointmentPageAdmin(1, 10, "NONEXISTENT");
        assertNotNull(none);
        assertEquals(0, none.getTotal());
    }
}
