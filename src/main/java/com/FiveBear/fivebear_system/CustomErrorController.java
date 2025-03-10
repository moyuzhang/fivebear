package com.FiveBear.fivebear_system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/error")
public class CustomErrorController {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            logger.error("Error occurred with status code: {}", statusCode);

            if (statusCode == 404) {
                logger.error("404 Not Found error occurred");
                return "error/404";
            } else if (statusCode == 500) {
                logger.error("500 Internal Server Error occurred");
                return "error/500";
            }
        } else {
            logger.error("Unknown error occurred");
        }
        return "error/general";
    }
}
