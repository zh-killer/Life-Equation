package com.lifeequation.service;

import com.lifeequation.config.TestConfig;
import com.lifeequation.entity.AiConsultation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * AI 智能导诊服务测试 — 验证症状分析、科室匹配、风险评估和 API 降级策略。
 *
 * 核心测试场景：
 * 1. DeepSeek API 正常返回时的解析流程
 * 2. API 不可用时的关键词匹配降级（兜底逻辑）
 * 3. 高中低三级风险识别的准确性
 */
@SpringBootTest
@Import(TestConfig.class)
@DisplayName("AI 智能导诊服务测试")
class AiConsultationServiceTest {

    @Autowired
    private AiConsultationService aiConsultationService;

    @MockBean
    private DeepSeekService deepSeekService;

    // ==================== 降级策略：科室匹配 ====================

    @Test
    @DisplayName("降级模式 — 头痛症状应匹配神经内科")
    void fallback_Headache_ShouldMatchNeurology() {
        when(deepSeekService.chat(anyString(), anyString())).thenReturn(null);

        AiConsultation result = aiConsultationService.createConsultation(1L, "头痛三天，伴有恶心");

        assertNotNull(result);
        assertEquals("神经内科", result.getSuggestedDepartment());
        assertNotNull(result.getAiAnalysis());
        assertNotNull(result.getHealthAdvice());
        assertNotNull(result.getRiskLevel());
        assertTrue(result.getRelatedDiseases().contains("头痛") || result.getRelatedDiseases().contains("偏头痛"));
    }

    @Test
    @DisplayName("降级模式 — 胸痛胸闷应匹配心内科")
    void fallback_ChestPain_ShouldMatchCardiology() {
        when(deepSeekService.chat(anyString(), anyString())).thenReturn(null);

        AiConsultation result = aiConsultationService.createConsultation(1L, "胸闷心慌，心跳加速");

        assertNotNull(result);
        assertEquals("心内科", result.getSuggestedDepartment());
        assertTrue(result.getRelatedDiseases().contains("心绞痛") || result.getRelatedDiseases().contains("冠心病"));
    }

    @Test
    @DisplayName("降级模式 — 咳嗽发热应匹配呼吸内科")
    void fallback_CoughFever_ShouldMatchRespiratory() {
        when(deepSeekService.chat(anyString(), anyString())).thenReturn(null);

        AiConsultation result = aiConsultationService.createConsultation(1L, "咳嗽一周，发烧38度");

        assertNotNull(result);
        assertEquals("呼吸内科", result.getSuggestedDepartment());
    }

    @Test
    @DisplayName("降级模式 — 无匹配关键词默认返回全科/内科")
    void fallback_NoMatch_ShouldDefaultToGeneral() {
        when(deepSeekService.chat(anyString(), anyString())).thenReturn(null);

        AiConsultation result = aiConsultationService.createConsultation(1L, "感觉有些疲劳");

        assertNotNull(result);
        assertEquals("全科/内科", result.getSuggestedDepartment());
        assertEquals("待进一步检查确认", result.getRelatedDiseases());
    }

    // ==================== 降级策略：风险评估 ====================

    @Test
    @DisplayName("降级模式 — 高危关键词（胸痛+呼吸困难）应评估为 HIGH")
    void fallback_HighRiskKeywords_ShouldReturnHigh() {
        when(deepSeekService.chat(anyString(), anyString())).thenReturn(null);

        AiConsultation result = aiConsultationService.createConsultation(1L, "剧烈胸痛，呼吸困难，伴有晕厥");

        assertNotNull(result);
        assertEquals("HIGH", result.getRiskLevel());
        assertTrue(result.getHealthAdvice().contains("立即"));
    }

    @Test
    @DisplayName("降级模式 — 中危关键词（持续头晕）应评估为 MEDIUM")
    void fallback_MediumRiskKeywords_ShouldReturnMedium() {
        when(deepSeekService.chat(anyString(), anyString())).thenReturn(null);

        AiConsultation result = aiConsultationService.createConsultation(1L, "持续头晕三天，反复加重");

        assertNotNull(result);
        assertEquals("MEDIUM", result.getRiskLevel());
    }

    @Test
    @DisplayName("降级模式 — 无风险关键词应评估为 LOW")
    void fallback_NoRiskKeyword_ShouldReturnLow() {
        when(deepSeekService.chat(anyString(), anyString())).thenReturn(null);

        AiConsultation result = aiConsultationService.createConsultation(1L, "偶尔打喷嚏，鼻子有点痒");

        assertNotNull(result);
        assertEquals("LOW", result.getRiskLevel());
    }

    // ==================== AI 正常返回路径 ====================

    @Test
    @DisplayName("API 正常 — 返回有效 JSON 时应正确解析")
    void apiSuccess_ValidJson_ShouldParseCorrectly() {
        String mockResponse = "{\n" +
                "  \"analysis\": \"根据您描述的头痛伴恶心症状，可能是偏头痛或紧张型头痛\",\n" +
                "  \"department\": \"神经内科\",\n" +
                "  \"riskLevel\": \"MEDIUM\",\n" +
                "  \"advice\": \"建议避免强光刺激，保持充足睡眠，尽快前往神经内科就诊\",\n" +
                "  \"diseases\": \"偏头痛,紧张型头痛,颈椎病\"\n" +
                "}";
        when(deepSeekService.chat(anyString(), anyString())).thenReturn(mockResponse);

        AiConsultation result = aiConsultationService.createConsultation(1L, "头痛三天，伴有恶心");

        assertNotNull(result);
        assertEquals("神经内科", result.getSuggestedDepartment());
        assertEquals("MEDIUM", result.getRiskLevel());
        assertTrue(result.getAiAnalysis().contains("偏头痛"));
        assertTrue(result.getHealthAdvice().contains("神经内科"));
        assertTrue(result.getRelatedDiseases().contains("偏头痛"));
    }

    // ==================== 历史记录查询 ====================

    @Test
    @DisplayName("查询历史 — 无记录时应返回空列表")
    void history_NoRecords_ShouldReturnEmpty() {
        var page = aiConsultationService.getHistoryPage(1, 10, 99999L);

        assertNotNull(page);
        assertEquals(0, page.getTotal());
    }
}
