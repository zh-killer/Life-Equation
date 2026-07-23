package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.AiHealthAdvice;
import com.lifeequation.mapper.AiHealthAdviceMapper;
import com.lifeequation.service.AiHealthAdviceService;
import com.lifeequation.service.DeepSeekService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class AiHealthAdviceServiceImpl extends ServiceImpl<AiHealthAdviceMapper, AiHealthAdvice> implements AiHealthAdviceService {

    private static final String SYSTEM_PROMPT =
        "你是一位专业的AI健康顾问。请根据用户提供的健康数据（年龄、身高、体重、性别、生活方式、病史、关注点），" +
        "生成一份个性化的健康评估报告。用中文返回，格式如下：\n" +
        "\n📊 您的健康评估报告\n\n" +
        "【基本信息】总结用户的基本情况\n" +
        "【BMI分析】计算并分析BMI（体重kg/(身高m)²），说明所处的体重范围及健康风险\n" +
        "【生活方式建议】针对用户的生活习惯给出3-5条具体建议\n" +
        "【重点关注】针对用户的健康关注点和既往病史给出针对性建议\n" +
        "【总结】一句话总结\n\n" +
        "⚠️ 免责声明：以上建议由AI生成，仅供参考，具体健康决策请咨询专业医生。\n\n" +
        "要求：语气专业但亲切，建议具体可执行，不要过于笼统。";

    private final DeepSeekService deepSeekService;

    public AiHealthAdviceServiceImpl(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @Override
    public AiHealthAdvice createAdvice(Long userId, AiHealthAdvice input) {
        input.setUserId(userId);
        input.setCreateTime(LocalDateTime.now());

        // Build user prompt
        StringBuilder userPrompt = new StringBuilder("以下是我的健康数据：\n");
        if (input.getAge() != null) userPrompt.append("年龄：").append(input.getAge()).append("岁\n");
        if (input.getHeight() != null) userPrompt.append("身高：").append(input.getHeight()).append("cm\n");
        if (input.getWeight() != null) userPrompt.append("体重：").append(input.getWeight()).append("kg\n");
        if (input.getGender() != null) userPrompt.append("性别：").append(input.getGender().equals("male") ? "男" : "女").append("\n");
        if (input.getLifestyle() != null && !input.getLifestyle().isBlank())
            userPrompt.append("生活方式：").append(input.getLifestyle()).append("\n");
        if (input.getMedicalHistory() != null && !input.getMedicalHistory().isBlank())
            userPrompt.append("既往病史：").append(input.getMedicalHistory()).append("\n");
        if (input.getConcerns() != null && !input.getConcerns().isBlank())
            userPrompt.append("健康关注：").append(input.getConcerns()).append("\n");

        // Try DeepSeek
        String aiResult = deepSeekService.chat(SYSTEM_PROMPT, userPrompt.toString());

        if (aiResult != null && !aiResult.isBlank()) {
            input.setAiAdvice(aiResult);
        } else {
            // Fallback: BMI-based template
            input.setAiAdvice(generateFallbackAdvice(input));
        }

        this.save(input);
        return input;
    }

    @Override
    public Page<AiHealthAdvice> getHistoryPage(int page, int size, Long userId) {
        LambdaQueryWrapper<AiHealthAdvice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiHealthAdvice::getUserId, userId)
               .orderByDesc(AiHealthAdvice::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public AiHealthAdvice getDetail(Long id) {
        AiHealthAdvice advice = this.getById(id);
        if (advice == null) throw new RuntimeException("健康建议记录不存在");
        return advice;
    }

    private String generateFallbackAdvice(AiHealthAdvice input) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 您的健康评估报告\n\n");

        sb.append("【基本信息】\n");
        if (input.getAge() != null) sb.append("年龄：").append(input.getAge()).append("岁\n");
        if (input.getGender() != null) sb.append("性别：").append(input.getGender().equals("male") ? "男" : "女").append("\n");
        if (input.getHeight() != null) sb.append("身高：").append(input.getHeight()).append("cm\n");
        if (input.getWeight() != null) sb.append("体重：").append(input.getWeight()).append("kg\n");

        // BMI
        String bmiCategory = "正常";
        if (input.getWeight() != null && input.getHeight() != null && input.getHeight().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal bmi = input.getWeight().divide(
                input.getHeight().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP).pow(2),
                1, RoundingMode.HALF_UP);
            double bmiVal = bmi.doubleValue();
            if (bmiVal < 18.5) bmiCategory = "偏瘦";
            else if (bmiVal < 24) bmiCategory = "正常";
            else if (bmiVal < 28) bmiCategory = "超重";
            else bmiCategory = "肥胖";
            sb.append("\n【BMI分析】您的BMI为").append(bmi).append("（").append(bmiCategory).append("）\n");

            switch (bmiCategory) {
                case "偏瘦" -> sb.append("建议增加蛋白质摄入并配合力量训练。\n");
                case "正常" -> sb.append("体重处于健康范围，请继续保持！\n");
                case "超重" -> sb.append("建议控制饮食热量，每周3-5次有氧运动。\n");
                case "肥胖" -> sb.append("建议咨询营养科医生制定科学减重计划。\n");
            }
        }

        if (input.getLifestyle() != null && !input.getLifestyle().isBlank()) {
            sb.append("\n【生活方式建议】\n");
            sb.append("• 保持规律作息，充足睡眠\n• 均衡饮食，多摄入蔬果\n• 适度运动，保持积极心态\n");
        }

        sb.append("\n---\n⚠️ 免责声明：以上建议由AI生成，仅供参考，具体健康决策请咨询专业医生。");
        return sb.toString();
    }
}
