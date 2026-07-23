package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.AiConsultation;
import com.lifeequation.mapper.AiConsultationMapper;
import com.lifeequation.service.AiConsultationService;
import com.lifeequation.service.DeepSeekService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AiConsultationServiceImpl extends ServiceImpl<AiConsultationMapper, AiConsultation> implements AiConsultationService {

    private static final Map<String, String> SYMPTOM_DEPT_MAP = new LinkedHashMap<>();
    private static final Map<String, String> HIGH_RISK_KEYWORDS = new LinkedHashMap<>();
    private static final Map<String, String> MEDIUM_RISK_KEYWORDS = new LinkedHashMap<>();

    static {
        SYMPTOM_DEPT_MAP.put("头痛|头晕|失眠|抽搐|癫痫|麻木|帕金森|脑血管|面瘫|三叉神经", "神经内科");
        SYMPTOM_DEPT_MAP.put("胸痛|胸闷|心悸|心慌|心脏|高血压|血压|冠心病|心绞痛", "心内科");
        SYMPTOM_DEPT_MAP.put("咳嗽|咳痰|发热|感冒|流感|喉咙|咽喉|扁桃体|肺炎|哮喘|支气管|呼吸", "呼吸内科");
        SYMPTOM_DEPT_MAP.put("腹痛|胃痛|腹泻|便秘|恶心|呕吐|反酸|烧心|消化|食欲|腹胀|嗳气", "消化内科");
        SYMPTOM_DEPT_MAP.put("腰痛|背痛|关节|骨折|骨|膝盖|颈椎|腰椎|肩周|扭伤|运动损伤", "骨科");
        SYMPTOM_DEPT_MAP.put("皮疹|瘙痒|过敏|湿疹|荨麻疹|皮肤|红斑|疱疹|痤疮|脱发", "皮肤科");
        SYMPTOM_DEPT_MAP.put("月经|白带|孕|产|妇科|子宫|卵巢|乳腺|乳房", "妇产科");
        SYMPTOM_DEPT_MAP.put("儿童|小儿|婴儿|生长|发育|疫苗", "儿科");
        SYMPTOM_DEPT_MAP.put("视力|眼睛|眼|盲|青光眼|白内障|近视|眼部", "眼科");
        SYMPTOM_DEPT_MAP.put("听力|耳鸣|耳|鼻窦|打鼾|声音", "耳鼻喉科");
        SYMPTOM_DEPT_MAP.put("牙|齿|口腔|牙龈|口臭|舌|嘴唇", "口腔科");
        SYMPTOM_DEPT_MAP.put("小便|尿|排尿|膀胱|肾|结石|水肿|蛋白尿|血尿", "泌尿外科");
        SYMPTOM_DEPT_MAP.put("焦虑|抑郁|情绪|心理|精神|压力|烦躁|恐惧", "精神科");
        SYMPTOM_DEPT_MAP.put("血糖|糖尿病|口渴|多饮|多尿|消瘦|甲状腺|肥胖|内分泌", "内分泌科");
        SYMPTOM_DEPT_MAP.put("贫血|血|白血病|淋巴|骨髓|血小板|出血|瘀斑", "血液内科");

        HIGH_RISK_KEYWORDS.put("胸痛|胸闷|呼吸困难|咯血|高热|抽搐|意识|晕厥|偏瘫|失语|剧烈", "HIGH");
        MEDIUM_RISK_KEYWORDS.put("持续|反复|加重|不退|血|肿|痛|发热|头晕|心悸", "MEDIUM");
    }

    private static final String SYSTEM_PROMPT =
        "你是一位经验丰富的医学AI助手，负责为用户提供症状分析。" +
        "请根据用户描述的症状，用中文返回以下格式的JSON（只返回JSON，不要其他文字）：\n" +
        "{\n" +
        "  \"analysis\": \"对症状的详细分析，包括可能的病因推断（100-200字）\",\n" +
        "  \"department\": \"建议就诊科室\",\n" +
        "  \"riskLevel\": \"LOW/MEDIUM/HIGH\",\n" +
        "  \"advice\": \"详细的就医和生活建议（100-200字）\",\n" +
        "  \"diseases\": \"可能关联的疾病，用逗号分隔\"\n" +
        "}\n" +
        "注意：只返回关联的可能疾病名称，不要写成诊断结论。必须包含免责声明：AI分析仅供参考，请务必就医确诊。";

    private final DeepSeekService deepSeekService;

    public AiConsultationServiceImpl(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @Override
    public AiConsultation createConsultation(Long userId, String symptomsInput) {
        AiConsultation consultation = new AiConsultation();
        consultation.setUserId(userId);
        consultation.setSymptomsInput(symptomsInput);
        consultation.setCreateTime(LocalDateTime.now());

        // Try DeepSeek first
        String aiResult = deepSeekService.chat(SYSTEM_PROMPT, "用户症状：" + symptomsInput);

        if (aiResult != null) {
            try {
                // Extract JSON from response (handle possible markdown wrapping)
                String json = aiResult.trim();
                if (json.startsWith("```")) {
                    json = json.replaceAll("```json?\\s*", "").replaceAll("```\\s*$", "");
                }
                // Try to parse key fields with simple extraction
                String analysis = extractField(json, "analysis");
                String department = extractField(json, "department");
                String riskLevel = extractField(json, "riskLevel");
                String advice = extractField(json, "advice");
                String diseases = extractField(json, "diseases");

                if (analysis != null && !analysis.isBlank()) {
                    consultation.setAiAnalysis(analysis);
                    consultation.setSuggestedDepartment(department != null ? department : matchDepartment(symptomsInput));
                    consultation.setRiskLevel(riskLevel != null ? riskLevel : assessRisk(symptomsInput));
                    consultation.setHealthAdvice(advice != null ? advice : generateAdviceFallback(department, riskLevel));
                    consultation.setRelatedDiseases(diseases != null ? diseases : guessDiseases(symptomsInput));
                    this.save(consultation);
                    return consultation;
                }
            } catch (Exception e) {
                System.err.println("[AI导诊] DeepSeek响应解析失败，使用兜底逻辑: " + e.getMessage());
            }
        }

        // Fallback to keyword matching
        String dept = matchDepartment(symptomsInput);
        String risk = assessRisk(symptomsInput);
        consultation.setAiAnalysis(generateAnalysisFallback(symptomsInput, dept, risk));
        consultation.setSuggestedDepartment(dept);
        consultation.setRiskLevel(risk);
        consultation.setHealthAdvice(generateAdviceFallback(dept, risk));
        consultation.setRelatedDiseases(guessDiseases(symptomsInput));

        this.save(consultation);
        return consultation;
    }

    private String extractField(String json, String field) {
        // Simple regex extraction for quoted values in JSON
        String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1).replace("\\n", "\n").replace("\\\"", "\"");
        }
        return null;
    }

    @Override
    public Page<AiConsultation> getHistoryPage(int page, int size, Long userId) {
        LambdaQueryWrapper<AiConsultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConsultation::getUserId, userId)
               .orderByDesc(AiConsultation::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public AiConsultation getDetail(Long id) {
        AiConsultation consultation = this.getById(id);
        if (consultation == null) throw new RuntimeException("AI问诊记录不存在");
        return consultation;
    }

    // ========== Fallback methods ==========

    private String matchDepartment(String symptoms) {
        for (Map.Entry<String, String> entry : SYMPTOM_DEPT_MAP.entrySet()) {
            for (String kw : entry.getKey().split("\\|")) {
                if (symptoms.contains(kw)) return entry.getValue();
            }
        }
        return "全科/内科";
    }

    private String assessRisk(String symptoms) {
        for (Map.Entry<String, String> entry : HIGH_RISK_KEYWORDS.entrySet()) {
            for (String kw : entry.getKey().split("\\|")) {
                if (symptoms.contains(kw)) return "HIGH";
            }
        }
        for (Map.Entry<String, String> entry : MEDIUM_RISK_KEYWORDS.entrySet()) {
            for (String kw : entry.getKey().split("\\|")) {
                if (symptoms.contains(kw)) return "MEDIUM";
            }
        }
        return "LOW";
    }

    private String generateAnalysisFallback(String symptoms, String dept, String risk) {
        String prefix = "根据您描述的症状「" + symptoms + "」，";
        if ("HIGH".equals(risk)) return prefix + "系统评估为高风险，建议立即就医。" + "可能涉及" + dept + "相关疾病。";
        if ("MEDIUM".equals(risk)) return prefix + "系统评估为中等风险，建议尽快前往" + dept + "就诊。";
        return prefix + "系统评估为低风险，建议前往" + dept + "进一步检查。";
    }

    private String generateAdviceFallback(String dept, String risk) {
        if ("HIGH".equals(risk)) return "⚠️ 请立即前往" + dept + "就诊！如症状急剧加重请拨打120。";
        if ("MEDIUM".equals(risk)) return "建议1-3天内前往" + dept + "就诊，注意休息和观察症状变化。";
        return "可先观察2-3天，若无改善请前往" + dept + "就诊。保持良好作息和饮食。";
    }

    private String guessDiseases(String symptoms) {
        StringBuilder sb = new StringBuilder();
        if (symptoms.contains("头痛") || symptoms.contains("头晕")) sb.append("紧张型头痛,偏头痛,");
        if (symptoms.contains("发热") || symptoms.contains("咳嗽")) sb.append("上呼吸道感染,支气管炎,");
        if (symptoms.contains("胸痛") || symptoms.contains("胸闷")) sb.append("冠心病,心绞痛,");
        if (symptoms.contains("腹痛") || symptoms.contains("胃痛")) sb.append("胃炎,消化性溃疡,");
        if (symptoms.contains("腰痛") || symptoms.contains("背痛")) sb.append("腰椎间盘突出,腰肌劳损,");
        if (symptoms.contains("皮疹") || symptoms.contains("瘙痒")) sb.append("湿疹,过敏性皮炎,");
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "待进一步检查确认";
    }
}
