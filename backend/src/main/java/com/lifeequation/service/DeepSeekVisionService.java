package com.lifeequation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;

/**
 * DeepSeek 视觉服务（deepseek-flash 原生多模态）
 * 单次调用：图片压缩 + 数据提取 + 医学解读
 *
 * 2026-09 起 DeepSeek V4.1-Flash 支持图片输入（input_modalities: text + image），
 * 与原智谱 GLM-4V 走同一套 OpenAI 兼容的 chat/completions 协议，故直接复用 deepseek.api 配置。
 */
@Service
public class DeepSeekVisionService {

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    /** 多模态模型。deepseek-chat 也会路由到同一个模型，但显式写 vision 模型更清楚 */
    @Value("${deepseek.api.vision-model:deepseek-flash}")
    private String model;

    /** 单次输出上限。关掉思考后 3000 足够输出完整四段式解读 */
    @Value("${deepseek.api.vision-max-tokens:3000}")
    private int maxTokens;

    /**
     * 应用访问基准 URL（用于按 URL 传图时拼出完整地址）。
     * 本地开发时为 http://localhost:8888，DeepSeek 服务器无法访问，需 ngrok 等内网穿透。
     */
    @Value("${app.base-url:http://localhost:8888}")
    private String appBaseUrl;

    private final RestTemplate restTemplate;

    public DeepSeekVisionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("YOUR_");
    }

    public String getModelName() {
        return model;
    }

    // ================================================================
    // 公开方法
    // ================================================================

    /**
     * 分析报告图片（base64 模式 — 推荐）。
     * 图片以 data URI 形式直接嵌入请求体，不需要 API 服务器回访公网 URL，
     * 本地开发、内网环境都能用。
     *
     * @param imageBytes 图片原始字节
     * @param mimeType   图片 MIME 类型 (e.g. "image/jpeg", "image/png")
     * @param reportType 报告类型
     * @return 解读文本，或 ApiError:xxx 格式的错误信息
     */
    public String analyzeReportFromBytes(byte[] imageBytes, String mimeType, String reportType) {
        if (!isConfigured() || imageBytes == null || imageBytes.length == 0) {
            return "ApiError:DeepSeek 未配置或无图片数据";
        }

        String actualMime = (mimeType != null && mimeType.startsWith("image/")) ? mimeType : "image/jpeg";
        byte[] compressed = compressImage(imageBytes, actualMime);
        // 压缩后统一按 JPEG 输出，MIME 也要跟着改，否则 API 解不出来
        if (compressed != imageBytes) actualMime = "image/jpeg";

        String dataUri = "data:" + actualMime + ";base64," + Base64.getEncoder().encodeToString(compressed);

        System.err.println("[DeepSeekVision] Base64 mode, image: " + imageBytes.length
            + " bytes → compressed: " + compressed.length + " bytes");
        System.err.println("[DeepSeekVision] Calling " + model + "...");
        return callVisionApi(buildPrompt(reportType), dataUri);
    }

    /**
     * 分析报告图片（URL 模式 — 备选）。
     * 需要图片能被打公网访问到；本地 localhost 地址会被直接拒绝。
     *
     * @param imageUrl   图片路径（如 /uploads/reports/1/xxx.jpg）或完整 http URL
     * @param reportType 报告类型
     * @return 解读文本，或 ApiError:xxx 格式的错误信息
     */
    public String analyzeReport(String imageUrl, String reportType) {
        if (!isConfigured() || imageUrl == null || imageUrl.isBlank()) {
            return "ApiError:DeepSeek 未配置或无图片URL";
        }

        String fullUrl = imageUrl.startsWith("http") ? imageUrl : (appBaseUrl + imageUrl);
        System.err.println("[DeepSeekVision] Image URL: " + fullUrl);

        if (fullUrl.contains("localhost") || fullUrl.contains("127.0.0.1")) {
            return "ApiError:图片URL为本地地址(" + fullUrl + ")，DeepSeek 服务器无法访问。"
                + "请改用 base64 模式上传图片，或用 ngrok 做内网穿透并修改 app.base-url。";
        }
        return callVisionApi(buildPrompt(reportType), fullUrl);
    }

    /**
     * 自检：现场画一张带检验数值的小图发给模型，验证 Key、网络、多模态能力是否都正常。
     * 不依赖任何外网图片，几十 KB，调用成本可以忽略。
     */
    public Map<String, Object> selfTest() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("model", model);
        status.put("configured", isConfigured());

        if (!isConfigured()) {
            status.put("status", "UNCONFIGURED");
            status.put("suggestion", "deepseek.api.key 为空，请检查 config/application-local.yml");
            return status;
        }

        byte[] png = buildTestImage();
        String result = callVisionApi("请读出图中第二行出现的那个带小数的数字，只回答数字本身，不要解释。",
            "data:image/png;base64," + Base64.getEncoder().encodeToString(png));

        status.put("testResult", result != null && result.length() > 200 ? result.substring(0, 200) : result);

        if (result == null || result.isBlank()) {
            status.put("status", "UNREACHABLE");
            status.put("suggestion", "API 完全无响应，请检查网络或 API Key 是否有效");
        } else if (result.startsWith("ApiError:")) {
            status.put("status", "ERROR");
            if (result.contains("401") || result.contains("unauthorized") || result.contains("invalid")) {
                status.put("suggestion", "API Key 无效或已过期，请到 https://platform.deepseek.com 重新获取");
            } else if (result.contains("402") || result.contains("Insufficient") || result.contains("余额")) {
                status.put("suggestion", "账户余额不足，请到 DeepSeek 开放平台充值");
            } else if (result.contains("429") || result.contains("quota") || result.contains("limit")) {
                status.put("suggestion", "请求频率超限或额度用尽，请稍后重试或检查账户余额");
            } else if (result.contains("Connection refused") || result.contains("timeout") || result.contains("UnknownHost")) {
                status.put("suggestion", "无法连接到 DeepSeek API 服务器，请检查网络");
            } else {
                status.put("suggestion", "请根据测试返回内容排查具体原因");
            }
        } else if (result.contains("12.5")) {
            status.put("status", "OK");
            status.put("suggestion", "视觉服务正常，能正确识别图片中的检验数值");
        } else {
            status.put("status", "DEGRADED");
            status.put("suggestion", "接口有响应，但没读出测试图里的 12.5，可能是模型改动或图片被压缩过度");
        }
        return status;
    }

    // ================================================================
    // 图片压缩
    // ================================================================
    private byte[] compressImage(byte[] input, String mimeType) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(input));
            if (img == null) {
                System.err.println("[DeepSeekVision] Cannot decode image, using raw bytes");
                return input;
            }

            // 限制最大尺寸：长边不超过 3072px（化验单需要保留小字细节）
            int w = img.getWidth();
            int h = img.getHeight();
            int max = 3072;
            if (w > max || h > max) {
                double ratio = Math.min((double) max / w, (double) max / h);
                w = (int) (w * ratio);
                h = (int) (h * ratio);
                BufferedImage scaled = new BufferedImage(w, h, img.getType() == BufferedImage.TYPE_CUSTOM
                    ? BufferedImage.TYPE_INT_RGB : img.getType());
                Graphics2D g = scaled.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(img, 0, 0, w, h, null);
                g.dispose();
                img = scaled;
                System.err.println("[DeepSeekVision] Resized to " + w + "x" + h);
            }

            // JPEG 压缩 92% 质量（化验单小字不能糊）
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.92f);
            writer.setOutput(new MemoryCacheImageOutputStream(out));
            writer.write(null, new IIOImage(img, null, null), param);
            writer.dispose();
            return out.toByteArray();

        } catch (Exception e) {
            System.err.println("[DeepSeekVision] Compression failed: " + e.getMessage() + ", using raw");
            return input;
        }
    }

    /** 画一张自检用的小图：白底黑字，第二行带一个固定数值 12.5 */
    private byte[] buildTestImage() {
        int width = 520, height = 160;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString("BLOOD TEST REPORT", 30, 60);
        g.drawString("WBC 12.5 x10^9/L   ref 4.0-10.0   H", 30, 115);
        g.dispose();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            return out.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    // ================================================================
    // Prompt
    // ================================================================
    private String buildPrompt(String reportType) {
        String label = switch (reportType) {
            case "blood_test" -> "血常规"; case "urine_test" -> "尿常规";
            case "liver_function" -> "肝功能"; case "kidney_function" -> "肾功能";
            case "blood_lipid" -> "血脂"; case "blood_sugar" -> "血糖";
            case "imaging" -> "影像报告"; default -> "体检报告";
        };

        return "【角色设定】你是一位资深医学检验科主任医师，拥有20年临床经验。现在有一位患者给你看一张"
            + label + "检验报告单的照片，请你仔细查看并给出专业解读。\n\n"
            + "【第一步：逐行提取数据】请仔细查看图片中的每一个文字和数字，以表格形式列出所有你看到的检验项目：\n"
            + "| 项目名称 | 检测结果 | 单位 | 参考范围 | 异常标记 |\n"
            + "|----------|----------|------|----------|----------|\n"
            + "| (示例) 白细胞WBC | 12.5 | ×10⁹/L | 4.0-10.0 | ↑ H |\n\n"
            + "识别规则：\n"
            + "- 逐行扫描，不遗漏任何一项，包括表格边角、页眉页脚的项目\n"
            + "- 精确识别小数点、正负号、科学计数法（如×10⁹/L、×10¹²/L）\n"
            + "- 标注图片中的异常标记（↑ ↓ * H L 红色字体等）\n"
            + "- 如果某个数字模糊看不清，标注为「模糊: 大致范围」但不要跳过\n"
            + "- 特别注意中英文混排的项目名称（如 WBC 白细胞、RBC 红细胞、HGB 血红蛋白）\n\n"
            + "【第二步：专业解读】提取完数据后，请以医生身份进行解读，严格使用以下四段式格式：\n\n"
            + "【关键发现】\n列出2-5个最值得关注的指标，每个都要写清：项目名称 + 具体数值 + 参考范围 + 偏离程度 + 可能的临床意义。如果所有指标都在正常范围内，请明确说明本次检验结果大致正常。\n\n"
            + "【逐项解读】\n对识别到的每一个检验项目逐一解释：该指标的含义、本次检测值的意义、偏高/偏低的常见原因。\n\n"
            + "【综合评估】\n给出整体判断（大致正常 / 部分指标异常建议复查 / 建议尽快就诊），并说明判断依据。\n\n"
            + "【就医建议】\n给出具体可操作的建议：饮食调整、运动建议、需要进一步做哪些检查、建议挂哪个科室等。\n\n"
            + "【重要】请直接开始识别和分析，不要说「图片不清晰」「无法识别」「需要更多信息」等推脱的话。如果图片确实非常模糊导致无法辨认大部分数据，请如实告知哪些部分看不清，但仍要尽力识别你能看到的内容。\n\n"
            + "⚠️ AI生成仅供参考，请咨询专业医生。";
    }

    // ================================================================
    // API 调用
    // ================================================================
    /**
     * @param userPrompt 用户提示
     * @param imageRef   图片地址：data URI 或 http URL
     */
    private String callVisionApi(String userPrompt, String imageRef) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String systemInstruction = "你是一个专业的医学检验报告解读助手。你的核心能力是从化验单图片中精准识别所有文字和数字，并基于临床参考范围进行专业解读。你必须仔细查看图片中的每一个数据，不要跳过任何项目。";

            Map<String, Object> user = new LinkedHashMap<>();
            user.put("role", "user");
            List<Map<String, Object>> content = new ArrayList<>();

            Map<String, Object> text = new LinkedHashMap<>();
            text.put("type", "text");
            text.put("text", systemInstruction + "\n\n" + userPrompt);
            content.add(text);

            Map<String, Object> img = new LinkedHashMap<>();
            img.put("type", "image_url");
            Map<String, String> url = new LinkedHashMap<>();
            url.put("url", imageRef);
            img.put("image_url", url);
            content.add(img);

            user.put("content", content);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            // 关掉思考：读单 + 结构化解读不需要长链推理，
            // 开着会把 max_tokens 全烧在 reasoning_tokens 上，导致正文被截断
            body.put("thinking", Map.of("type", "disabled"));
            body.put("max_tokens", maxTokens);
            body.put("messages", List.of(user));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> resp = restTemplate.postForEntity(apiUrl, entity, Map.class);

            System.err.println("[DeepSeekVision] HTTP status: " + resp.getStatusCode().value());

            Map<String, Object> respBody = resp.getBody();
            if (respBody == null) {
                return "ApiError:HTTP " + resp.getStatusCode().value() + " — 响应体为空";
            }

            if (respBody.containsKey("error")) {
                Object err = respBody.get("error");
                String errStr = err instanceof Map ? err.toString() : String.valueOf(err);
                System.err.println("[DeepSeekVision] API error: " + errStr);
                return "ApiError:DeepSeek API错误(HTTP " + resp.getStatusCode().value() + ") — " + errStr;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) respBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "ApiError:响应中没有 choices";
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
            if (msg == null) {
                return "ApiError:响应中没有 message";
            }

            String contentResult = (String) msg.get("content");
            if (contentResult == null || contentResult.isBlank()) {
                String finish = String.valueOf(choices.get(0).get("finish_reason"));
                return "ApiError:返回内容为空（finish_reason=" + finish + "，可能是 max_tokens 不足）";
            }

            System.err.println("[DeepSeekVision] Success, " + contentResult.length() + " chars");
            return contentResult;

        } catch (Exception e) {
            String detail = e.getMessage();
            if (detail == null) detail = e.getClass().getSimpleName();
            Throwable cause = e.getCause();
            String causeMsg = cause != null ? cause.getMessage() : null;
            if (causeMsg != null && !causeMsg.equals(detail)) {
                detail = detail + " [Cause: " + causeMsg + "]";
            }
            if (detail.length() > 500) detail = detail.substring(0, 500);
            System.err.println("[DeepSeekVision] Exception: " + detail);
            e.printStackTrace(System.err);
            return "ApiError:调用失败 — " + detail;
        }
    }
}
