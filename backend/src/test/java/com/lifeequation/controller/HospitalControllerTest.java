package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeequation.config.TestConfig;
import com.lifeequation.entity.Hospital;
import com.lifeequation.service.HospitalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller 层集成测试（HospitalController）— 公开 API 端点。
 *
 * 使用 @SpringBootTest + @AutoConfigureMockMvc 验证控制器的路由映射、
 * 参数绑定、HTTP 状态码和 JSON 响应结构。
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
@DisplayName("医院控制器集成测试")
class HospitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HospitalService hospitalService;

    // ==================== GET /api/hospitals ====================

    @Test
    @DisplayName("GET /api/hospitals — 默认分页查询，返回200")
    void list_WithDefaultParams_ShouldReturn200() throws Exception {
        Page<Hospital> mockPage = new Page<>(1, 10);
        mockPage.setTotal(2);
        mockPage.setRecords(Arrays.asList(
                createHospital(1L, "第一人民医院", "上海", "三甲"),
                createHospital(2L, "中心医院", "北京", "三甲")
        ));

        when(hospitalService.getHospitalPage(1, 10, null, null, null))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/hospitals")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records", hasSize(2)))
                .andExpect(jsonPath("$.data.records[0].name").value("第一人民医院"))
                .andExpect(jsonPath("$.data.records[1].city").value("北京"))
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    @DisplayName("GET /api/hospitals — 带搜索关键字的查询")
    void list_WithKeyword_ShouldPassParams() throws Exception {
        Page<Hospital> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(createHospital(1L, "人民医院", "广州", "三甲")));

        when(hospitalService.getHospitalPage(1, 10, "人民", null, null))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/hospitals")
                        .param("keyword", "人民")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].name").value("人民医院"));
    }

    @Test
    @DisplayName("GET /api/hospitals — 带分页参数和筛选条件")
    void list_WithPageAndFilters_ShouldPassParams() throws Exception {
        Page<Hospital> mockPage = new Page<>(2, 5);
        mockPage.setRecords(List.of());

        when(hospitalService.getHospitalPage(2, 5, null, "三甲", "上海"))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/hospitals")
                        .param("page", "2")
                        .param("size", "5")
                        .param("level", "三甲")
                        .param("city", "上海")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isEmpty());
    }

    // ==================== GET /api/hospitals/cities ====================

    @Test
    @DisplayName("GET /api/hospitals/cities — 返回城市列表")
    void cities_ShouldReturn200() throws Exception {
        when(hospitalService.getHospitalCities())
                .thenReturn(Arrays.asList("上海", "北京", "广州", "深圳"));

        mockMvc.perform(get("/api/hospitals/cities")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[0]").value("上海"))
                .andExpect(jsonPath("$.data[3]").value("深圳"));
    }

    // ==================== GET /api/hospitals/{id} ====================

    @Test
    @DisplayName("GET /api/hospitals/{id} — 查询存在的医院详情，返回200")
    void detail_WhenExists_ShouldReturn200() throws Exception {
        Hospital hospital = createHospital(1L, "第一人民医院", "上海", "三甲");
        hospital.setAddress("上海市黄浦区");
        hospital.setIntroduction("市级综合医院");

        when(hospitalService.getHospitalDetail(1L)).thenReturn(hospital);

        mockMvc.perform(get("/api/hospitals/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("第一人民医院"))
                .andExpect(jsonPath("$.data.city").value("上海"))
                .andExpect(jsonPath("$.data.address").value("上海市黄浦区"));
    }

    @Test
    @DisplayName("GET /api/hospitals/{id} — 查询不存在的医院，data为null")
    void detail_WhenNotExists_ShouldHandleGracefully() throws Exception {
        when(hospitalService.getHospitalDetail(999L)).thenReturn(null);

        mockMvc.perform(get("/api/hospitals/{id}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ==================== Helper ====================

    private Hospital createHospital(Long id, String name, String city, String level) {
        Hospital h = new Hospital();
        h.setId(id);
        h.setName(name);
        h.setCity(city);
        h.setLevel(level);
        h.setStatus(1);
        return h;
    }
}
