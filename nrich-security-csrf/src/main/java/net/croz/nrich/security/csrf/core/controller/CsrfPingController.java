package net.croz.nrich.security.csrf.core.controller;

import net.croz.nrich.security.csrf.core.constants.CsrfConstants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Used by client for csrf ping url
 */
@RequestMapping
public class CsrfPingController {

    private static final String SUCCESS_KEY = "success";

    @RequestMapping("${nrich.security.csrf.endpoint-path:" + CsrfConstants.CSRF_DEFAULT_PING_URI + "}")
    @ResponseBody
    public Map<String, Boolean> ping() {
        Map<String, Boolean> result = new HashMap<>();

        result.put(SUCCESS_KEY, true);

        return result;
    }
}
