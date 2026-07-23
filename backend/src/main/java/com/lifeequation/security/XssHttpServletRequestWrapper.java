package com.lifeequation.security;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * Request wrapper that sanitizes parameters against XSS attacks.
 * Uses Hutool's HtmlUtil to escape HTML special characters.
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return cleanXSS(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        String[] cleaned = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            cleaned[i] = cleanXSS(values[i]);
        }
        return cleaned;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return cleanXSS(value);
    }

    private String cleanXSS(String value) {
        if (StrUtil.isBlank(value)) return value;
        // Strip dangerous script/event tags
        value = value.replaceAll("(?i)<script.*?>.*?</script>", "");
        value = value.replaceAll("(?i)<script.*?>", "");
        value = value.replaceAll("(?i)</script>", "");
        value = value.replaceAll("(?i)javascript:", "");
        value = value.replaceAll("(?i)on\\w+\\s*=", "_disabled=");
        value = value.replaceAll("(?i)<iframe.*?>.*?</iframe>", "");
        value = value.replaceAll("(?i)<link.*?>", "");
        // Encode HTML entities to prevent script injection
        return cn.hutool.http.HtmlUtil.filter(value);
    }
}
