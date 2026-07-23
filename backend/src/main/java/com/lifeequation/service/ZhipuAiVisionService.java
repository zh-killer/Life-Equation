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
import java.io.OutputStream;
import java.util.*;
import java.util.List;

/**
 * 智谱AI GLM-4V 视觉服务
 * 单次调用：图片压缩 + 数据提取 + 医学解读
 */
@Service
public class ZhipuAiVisionService {

    @Value("${zhipuai.api.key:}")
    private String apiKey;

    @Value("${zhipuai.api.url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String apiUrl;

    @Value("${zhipuai.api.model:glm-4v-flash}")
    private String model;

    /**
     * 应用访问基准 URL（用于智谱 API 访问上传的图片）。
     * 本地开发时配置为 http://localhost:8888（需要 ngrok 等工具或智谱能访问 localhost）。
     * 生产环境配置为公网域名。
     */
    @Value("${app.base-url:http://localhost:8888}")
    private String appBaseUrl;

    private final RestTemplate restTemplate;

    public ZhipuAiVisionService(RestTemplate restTemplate) {
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
     * 图片以 data URI 形式直接嵌入 API 请求体，无需智谱服务器下载外部 URL。
     * 适用于本地开发、内网环境等没有公网 IP 的场景。
     *
     * @param imageBytes 图片原始字节
     * @param mimeType   图片 MIME 类型 (e.g. "image/jpeg", "image/png")
     * @param reportType 报告类型
     * @return 解读文本，或 ApiError:xxx 格式的错误信息
     */
    public String analyzeReportFromBytes(byte[] imageBytes, String mimeType, String reportType) {
        if (!isConfigured() || imageBytes == null || imageBytes.length == 0) {
            return "ApiError:智谱AI未配置或无图片数据";
        }

        // 压缩图片以减少 base64 体积（GLM-4V 对图片大小有限制）
        byte[] compressed = compressImage(imageBytes, mimeType);
        String base64 = Base64.getEncoder().encodeToString(compressed);
        String dataUri = "data:" + (mimeType != null ? mimeType : "image/jpeg") + ";base64," + base64;

        System.err.println("[ZhipuAI] Base64 mode, image size: " + imageBytes.length
            + " bytes → compressed: " + compressed.length + " bytes → base64: " + base64.length() + " chars");

        String userPrompt = buildPrompt(reportType);
        System.err.println("[ZhipuAI] Calling " + model + " with base64 data URI...");
        return callVisionApi(userPrompt, dataUri);
    }

    /**
     * 分析报告图片（URL 模式 — 备选）。
     * GLM-4V-Flash 免费版只支持 HTTP URL，不支持 base64 data URI。
     * 调用前会检测图片 URL 是否能从公网访问，如果不可达会直接返回错误提示。
     *
     * @param imageUrl   图片路径（如 /uploads/reports/1/xxx.jpg）
     * @param reportType 报告类型
     * @return 解读文本，或 ApiError:xxx 格式的错误信息
     */
    public String analyzeReport(String imageUrl, String reportType) {
        if (!isConfigured() || imageUrl == null || imageUrl.isBlank()) {
            return "ApiError:智谱AI未配置或无图片URL";
        }

        String fullUrl = imageUrl.startsWith("http") ? imageUrl : (appBaseUrl + imageUrl);
        System.err.println("[ZhipuAI] Image URL: " + fullUrl);

        // 预检：如果 URL 是 localhost/127.0.0.1，智谱服务器无法访问，直接提示用户
        if (fullUrl.contains("localhost") || fullUrl.contains("127.0.0.1")) {
            return "ApiError:图片URL为本地地址(" + fullUrl + ")，智谱API无法访问。"
                + "请用 ngrok 做内网穿透（ngrok http 8888），"
                + "然后把 application.yml 中的 app.base-url 改为 ngrok 提供的公网URL。";
        }

        String userPrompt = buildPrompt(reportType);
        System.err.println("[ZhipuAI] Calling " + model + " with URL...");
        return callVisionApi(userPrompt, fullUrl);
    }

    /**
     * 保留旧签名（兼容性）：直接使用 base64 模式。
     * @deprecated 请使用 {@link #analyzeReportFromBytes(byte[], String, String)}
     */
    @Deprecated
    public String analyzeReport(byte[] rawBytes, String mimeType, String reportType) {
        return analyzeReportFromBytes(rawBytes, mimeType, reportType);
    }

    // ================================================================
    // 图片压缩
    // ================================================================
    private byte[] compressImage(byte[] input, String mimeType) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(input));
            if (img == null) {
                System.err.println("[ZhipuAI] Cannot decode image, using raw bytes");
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
                System.err.println("[ZhipuAI] Resized to " + w + "x" + h);
            }

            // JPEG 压缩 92% 质量（保留更多细节，化验单小字不能糊）
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
            System.err.println("[ZhipuAI] Compression failed: " + e.getMessage() + ", using raw");
            return input;
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
     * @param imageUrl  图片的 HTTP URL（不是 data URI）
     */
    private String callVisionApi(String userPrompt, String imageUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // system instruction — 智谱 GLM-4 系列支持 system message
            // 但某些旧版 / vision 模式下可能不支持，改为合并在 user prompt 最前面
            String systemInstruction = "你是一个专业的医学检验报告解读助手。你的核心能力是从化验单图片中精准识别所有文字和数字，并基于临床参考范围进行专业解读。你必须仔细查看图片中的每一个数据，不要跳过任何项目。";

            // user — system instruction + text query + image
            Map<String, Object> user = new LinkedHashMap<>();
            user.put("role", "user");
            List<Map<String, Object>> content = new ArrayList<>();

            // 将 system instruction 作为 user 文本的首段
            Map<String, Object> sysAsText = new LinkedHashMap<>();
            sysAsText.put("type", "text");
            sysAsText.put("text", systemInstruction + "\n\n" + userPrompt);
            content.add(sysAsText);

            Map<String, Object> img = new LinkedHashMap<>();
            img.put("type", "image_url");
            Map<String, String> url = new LinkedHashMap<>();
            url.put("url", imageUrl);
            img.put("image_url", url);
            content.add(img);

            user.put("content", content);

            // request body — GLM-4V-Flash 不支持 temperature/max_tokens
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("messages", List.of(user));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> resp = restTemplate.postForEntity(apiUrl, entity, Map.class);

            // 诊断：打印 HTTP 状态码
            System.err.println("[ZhipuAI] HTTP status: " + resp.getStatusCode().value());

            Map<String, Object> respBody = resp.getBody();
            if (respBody == null) {
                return "ApiError:HTTP " + resp.getStatusCode().value() + " — 响应体为空";
            }

            // 检查 API 错误
            if (respBody.containsKey("error")) {
                Object err = respBody.get("error");
                String errStr = err instanceof Map ? err.toString() : String.valueOf(err);
                System.err.println("[ZhipuAI] API error: " + errStr);
                return "ApiError:智谱API错误(HTTP " + resp.getStatusCode().value() + ") — " + errStr;
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

            String content_result = (String) msg.get("content");
            if (content_result == null || content_result.isBlank()) {
                return "ApiError:返回内容为空";
            }

            System.err.println("[ZhipuAI] Success, " + content_result.length() + " chars");
            return content_result;

        } catch (Exception e) {
            // 捕获完整异常链，方便诊断
            String detail = e.getMessage();
            if (detail == null) detail = e.getClass().getSimpleName();
            // 尝试获取 cause 信息
            Throwable cause = e.getCause();
            String causeMsg = cause != null ? cause.getMessage() : null;
            if (causeMsg != null && !causeMsg.equals(detail)) {
                detail = detail + " [Cause: " + causeMsg + "]";
            }
            if (detail.length() > 500) detail = detail.substring(0, 500);
            System.err.println("[ZhipuAI] Exception: " + detail);
            e.printStackTrace(System.err);
            return "ApiError:调用失败 — " + detail;
        }
    }
}
