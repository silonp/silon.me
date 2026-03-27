package me.silon.personal_app.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.RequestDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class GlobalErrorController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            log.info("Status Code = {}", statusCode);

            if (statusCode == 404) {
                return "forward:/error/404.html";
            }
        }
        log.info("Fall-back to default error page");
        return "forward:/error/error.html";
    }
}