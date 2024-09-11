package org.example.expert.domain.user.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAccessAspect {

    @Pointcut("@annotation(org.example.expert.domain.user.annotation.AdminAccess)")
    private void adminAccess() {
    }

    @Around("adminAccess()")
    public Object logAdminAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("RequestAttributes가 사용 불가능합니다.");
            return joinPoint.proceed();
        }

        HttpServletRequest request = (HttpServletRequest) attributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        if (request == null) {
            log.warn("HttpServletRequest가 null입니다.");
            return joinPoint.proceed();
        }

        // userId를 문자열로 가져오고, Long으로 변환
        String userIdStr = (String) request.getAttribute("userId");
        Long userId = (userIdStr != null) ? Long.parseLong(userIdStr) : null;

        String requestUrl = request.getRequestURI();
        long requestTime = System.currentTimeMillis();

        log.info("API 요청 시각: {}", requestTime);
        log.info("요청한 사용자의 ID: {}", userId);
        log.info("API 요청 URL: {}", requestUrl);

        Object result;
        try {
            result = joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("요청 종료 시각: {}", endTime);
            log.info("요청 소요 시간: {} ms", endTime - requestTime);
        }

        return result;
    }
}