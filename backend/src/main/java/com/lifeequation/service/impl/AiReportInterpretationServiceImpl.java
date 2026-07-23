package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.AiReportInterpretation;
import com.lifeequation.mapper.AiReportInterpretationMapper;
import com.lifeequation.service.AiReportInterpretationService;
import com.lifeequation.service.DeepSeekService;
import com.lifeequation.service.ZhipuAiVisionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class AiReportInterpretationServiceImpl extends ServiceImpl<AiReportInterpretationMapper, AiReportInterpretation> implements AiReportInterpretationService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /**
     * DeepSeek 纯文本解读 prompt（仅当智谱视觉不可用时使用）
     */
    private static final String TEXT_FALLBACK_PROMPT =
        "你是一位拥有20年临床经验的医学检验科主任医师。请对以下检验数据进行专业解读。\n\n"
        + "请严格按照以下格式输出完整报告：\n\n"
        + "【关键发现】\n列出2-5个最值得关注的指标及其异常程度\n\n"
        + "【逐项解读】\n对每个检验项目逐一解释含义和临床意义\n\n"
        + "【综合评估】\n整体判断（大致正常 / 建议复查 / 建议就诊）及详细依据\n\n"
        + "【就医建议】\n生活调整、复查建议、就诊科室等具体可操作的建议\n\n"
        + "⚠️ AI生成仅供参考，请咨询专业医生。";

    /**
     * 视觉识别成功后，要求 DeepSeek 精炼优化智谱原始结果的 prompt（可选增强）
     */
    private static final String ENRICH_PROMPT =
        "你是一位资深医学检验科主任。以下是一份AI初步生成的体检报告解读，请以专业医生的口吻进行精炼和优化，"
        + "补充遗漏的临床意义、可能的鉴别诊断方向，并确保建议具体可操作。保持四段式输出格式不变。";

    private final DeepSeekService deepSeekService;
    private final ZhipuAiVisionService visionService;

    public AiReportInterpretationServiceImpl(DeepSeekService deepSeekService,
                                              ZhipuAiVisionService visionService) {
        this.deepSeekService = deepSeekService;
        this.visionService = visionService;
    }

    @Override
    public AiReportInterpretation createInterpretation(Long userId, MultipartFile image, String reportType, String reportData) {
        AiReportInterpretation report = new AiReportInterpretation();
        report.setUserId(userId);
        report.setReportType(reportType != null ? reportType : "other");
        report.setCreateTime(LocalDateTime.now());

        String reportLabel = getReportTypeLabel(reportType);

        // 1. 先获取图片字节（用于 AI 识别），同时保存到磁盘（用于存档）
        byte[] imageBytes;
        String mimeType = image.getContentType();
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "image/jpeg";
        }
        try {
            imageBytes = image.getBytes();
            System.err.println("[Report] Image bytes: " + imageBytes.length + ", mime: " + mimeType);
        } catch (Exception e) {
            throw new RuntimeException("读取图片数据失败: " + e.getMessage());
        }

        // 保存图片到服务器（用于前端展示和存档）
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String originalFilename = image.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            String dir = uploadPath + File.separator + "reports" + File.separator + userId;
            File dirFile = new File(dir);
            if (!dirFile.exists()) dirFile.mkdirs();
            image.transferTo(new File(dir, filename));
            report.setImageUrl("/uploads/reports/" + userId + "/" + filename);
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }

        // 2. 优先：智谱 GLM-4V 使用 base64 模式直接识别图片（无需公网 URL）
        String aiResult = null;
        boolean usedVision = false;
        String visionError = null;

        if (visionService.isConfigured()) {
            System.err.println("[Report] Using " + visionService.getModelName() + " base64 mode for analysis...");
            aiResult = visionService.analyzeReportFromBytes(imageBytes, mimeType, reportType);
            if (aiResult != null && !aiResult.isBlank()) {
                if (aiResult.startsWith("ApiError:")) {
                    visionError = aiResult.substring("ApiError:".length()).trim();
                    System.err.println("[Report] Vision API error: " + visionError);
                    aiResult = null; // 清空，走降级逻辑
                } else {
                    usedVision = true;
                    report.setOcrText(reportLabel + " — 由 " + visionService.getModelName() + " 从图片中自动识别");
                }
            } else {
                System.err.println("[Report] Vision analysis returned empty, will try DeepSeek fallback");
            }
        } else {
            System.err.println("[Report] ZhipuAI not configured");
        }

        // 3. 降级：智谱不可用时，用 DeepSeek + 用户手动输入的数据
        if (!usedVision) {
            StringBuilder sb = new StringBuilder();
            if (reportData != null && !reportData.isBlank()) {
                // 用户手动输入了数据 → DeepSeek 直接解读文本数据
                sb.append("以下是一位用户的").append(reportLabel).append("检验数据，请逐项解读：\n\n")
                  .append(reportData);
            } else {
                // 用户未输入数据，且图片识别失败 → 告知用户需要手动输入
                sb.append("用户上传了一份").append(reportLabel)
                  .append("报告图片，但AI视觉识别未能自动提取图片中的数据。\n\n")
                  .append("请告知用户：\n")
                  .append("1. 建议对照报告单，在「手动补充数据」区域输入检验项目的名称和数值\n")
                  .append("2. 输入格式示例：白细胞计数(WBC) 12.5 ×10⁹/L ↑\n")
                  .append("3. 同时，基于该报告类型，简要说明常见检验项目和参考范围，帮助用户了解需要输入哪些数据\n\n")
                  .append("并以四段式格式（关键发现/逐项解读/综合评估/就医建议）给出解读框架。");
            }
            aiResult = deepSeekService.chat(TEXT_FALLBACK_PROMPT, sb.toString());
            if (aiResult == null || aiResult.isBlank()) {
                aiResult = deepSeekService.chat(TEXT_FALLBACK_PROMPT,
                    "请对" + reportLabel + "报告进行通用解读，说明常见检验项目及参考范围。");
            }
            report.setOcrText(reportLabel + "报告图片已存档"
                + (reportData != null && !reportData.isBlank() ? "，用户已手动输入数据" : "（⚠️ AI未能从图片中自动识别数据，请对照报告单手动输入）")
                + (visionError != null ? " | 视觉识别失败原因: " + visionError : ""));
        }

        // 4. 解析并保存结果
        if (aiResult != null && !aiResult.isBlank()) {
            report.setAiInterpretation(aiResult);
            report.setKeyFindings(extractSection(aiResult, "【关键发现】", "【逐项解读】"));
            String assessment = extractSection(aiResult, "【综合评估】", "【就医建议】");
            report.setRiskAssessment(evaluateRisk(assessment, report.getKeyFindings()));
            report.setRecommendations(extractSection(aiResult, "【就医建议】", "⚠️"));
        } else {
            report.setAiInterpretation("很抱歉，AI 分析服务当前不可用。\n\n"
                + "您的" + reportLabel + "报告图片已安全保存。可能的原因：\n"
                + "1. AI 视觉识别服务（智谱" + visionService.getModelName() + "）API Key 无效或已过期\n"
                + "2. 文本分析服务（DeepSeek）暂不可用\n"
                + "3. 网络连接问题\n\n"
                + "请检查 API Key 配置后重试，或将检验数据手动输入到报告数据区域。");
            report.setKeyFindings("（AI 服务不可用，无法分析）");
            report.setRiskAssessment("caution");
            report.setRecommendations("请将检验报告带给社区医院或体检中心医生进行专业解读。");
        }

        this.save(report);
        return report;
    }

    @Override
    public Page<AiReportInterpretation> getHistoryPage(int page, int size, Long userId) {
        LambdaQueryWrapper<AiReportInterpretation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiReportInterpretation::getUserId, userId)
               .orderByDesc(AiReportInterpretation::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public AiReportInterpretation getDetail(Long id) {
        AiReportInterpretation report = this.getById(id);
        if (report == null) throw new RuntimeException("报告解读记录不存在");
        return report;
    }

    @Override
    public java.util.Map<String, Object> diagnoseVisionStatus() {
        java.util.Map<String, Object> status = new java.util.LinkedHashMap<>();
        status.put("model", visionService.getModelName());
        status.put("configured", visionService.isConfigured());

        // 用一张公开可访问的测试图 URL 测试 API 连通性
        // (GLM-4V-Flash 免费版只支持 URL，不支持 base64。图片需足够大，否则 API 报 1210)
        String testImageUrl = "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png";
        String result = visionService.analyzeReport(testImageUrl, "other");
        status.put("testResult", result != null ? result.substring(0, Math.min(300, result.length())) : "null");

        if (result == null || result.isBlank()) {
            status.put("status", "UNREACHABLE");
            status.put("suggestion", "API 完全无响应，请检查网络或 API Key 是否有效");
        } else if (result.startsWith("ApiError:")) {
            status.put("status", "ERROR");
            status.put("errorDetail", result);
            if (result.contains("401") || result.contains("unauthorized") || result.contains("invalid")) {
                status.put("suggestion", "API Key 无效或已过期，请到 https://open.bigmodel.cn 重新获取");
            } else if (result.contains("429") || result.contains("quota") || result.contains("limit")) {
                status.put("suggestion", "API 调用额度已用完或频率限制，请检查账户余额");
            } else if (result.contains("Connection refused") || result.contains("timeout") || result.contains("UnknownHost")) {
                status.put("suggestion", "无法连接到智谱 API 服务器，请检查网络");
            } else {
                status.put("suggestion", "请根据 errorDetail 排查具体原因");
            }
        } else {
            status.put("status", "OK");
            status.put("suggestion", "视觉服务正常，可以识别图片");
        }

        // DeepSeek 状态
        status.put("deepSeekConfigured", deepSeekService.isConfigured());
        return status;
    }

    private String getReportTypeLabel(String type) {
        return switch (type) {
            case "blood_test" -> "血常规";
            case "urine_test" -> "尿常规";
            case "liver_function" -> "肝功能";
            case "kidney_function" -> "肾功能";
            case "blood_lipid" -> "血脂";
            case "blood_sugar" -> "血糖";
            case "imaging" -> "影像报告";
            default -> "体检报告";
        };
    }

    /**
     * 智能评估风险等级。
     * 基于关键发现（keyFindings）中的异常标记进行评分，综合评估（assessment）作为辅助判断。
     *
     * 评估原则：
     * - 先统计关键发现中的异常项目数量和严重程度
     * - 再结合综合评估中的临床判断（建议就诊 > 建议复查 > 大致正常）
     * - 避免被「除XX外大致正常」这类句式误导
     */
    private String evaluateRisk(String assessment, String keyFindings) {
        if (assessment == null && keyFindings == null) return "normal";

        String combined = ((keyFindings != null ? keyFindings : "") + "\n"
                          + (assessment != null ? assessment : "")).toLowerCase();

        // │ 第一层：关键发现中的异常标记计数
        int abnormalScore = 0;

        // 明确的异常标记（每个 +2 分）
        String[] criticalMarkers = {"↑", "h ", "高", "异常升高", "显著升高", "明显升高",
            "严重超标", "危急", "阳性", "+++", "++", "重度"};
        for (String m : criticalMarkers) {
            int idx = -1;
            while ((idx = combined.indexOf(m.toLowerCase(), idx + 1)) >= 0) {
                abnormalScore += 2;
            }
        }

        // 偏高/偏低标记（每个 +1 分）
        String[] mildMarkers = {"偏高", "偏低", "增高", "降低", "下降", "↓", "l ",
            "超出正常", "低于正常", "轻度异常", "+", "*"};
        for (String m : mildMarkers) {
            int idx = -1;
            while ((idx = combined.indexOf(m.toLowerCase(), idx + 1)) >= 0) {
                abnormalScore += 1;
            }
        }

        // │ 第二层：综合评估中的临床建议信号
        boolean hasUrgentSignal = combined.contains("建议就诊") || combined.contains("及时就诊")
                || combined.contains("尽快就诊") || combined.contains("立即就医")
                || combined.contains("建议住院");

        boolean hasCautionSignal = combined.contains("建议复查") || combined.contains("定期复查")
                || combined.contains("进一步检查") || combined.contains("随访观察")
                || combined.contains("动态监测");

        // 纯粹的「全部正常」信号（排除「除XX外大致正常」这类）
        boolean hasPureNormalSignal = false;
        if (assessment != null) {
            String assess = assessment;
            // 只有当正常信号出现，且异常信号很少时才是真的正常
            boolean saysNormal = assess.contains("大致正常") || assess.contains("未见明显异常")
                    || assess.contains("未见异常") || assess.contains("无明显异常")
                    || assess.contains("正常范围") || assess.contains("均在正常")
                    || assess.contains("无需就诊") || assess.contains("不用就诊")
                    || assess.contains("暂无异常");
            hasPureNormalSignal = saysNormal && abnormalScore < 2 && !hasUrgentSignal;
        }

        // │ 第三层：综合判定
        if (hasUrgentSignal || abnormalScore >= 6) {
            return "abnormal";
        }
        if (abnormalScore >= 3 || hasCautionSignal) {
            return "caution";
        }
        if (hasPureNormalSignal) {
            return "normal";
        }

        // 兜底：有关键发现但无法归类 → caution
        if (keyFindings != null && !keyFindings.isBlank()
            && !keyFindings.contains("（AI 服务不可用")) {
            return "caution";
        }
        return "normal";
    }

    private String extractSection(String text, String startMarker, String endMarker) {
        if (text == null) return null;
        int start = text.indexOf(startMarker);
        if (start < 0) return null;
        start += startMarker.length();
        int end = endMarker != null ? text.indexOf(endMarker, start) : -1;
        String result = end > start ? text.substring(start, end).trim() : text.substring(start).trim();
        return result.replaceAll("^[\\s\\n]+", "").replaceAll("[\\s\\n\\r]+$", "");
    }
}
