package org.kreyzon.springops.audits.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.kreyzon.springops.audits.service.AuditService;
import org.kreyzon.springops.common.dto.audits.AuditDto;
import org.kreyzon.springops.common.enums.AuditResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect for auditing service method calls.
 * This aspect intercepts only methods explicitly annotated with @Audit.
 *
 * @author Lorenzo Orlando
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    /**
     * Pointcut that matches methods annotated with @Audit.
     */
    @Pointcut("@annotation(org.kreyzon.springops.config.Audit)")
    public void auditedMethods() {}

    /**
     * Advice that runs after a method annotated with @Audit is executed successfully.
     *
     * @param joinPoint the join point representing the method call
     * @param result    the result of the method execution
     */
    @AfterReturning(pointcut = "auditedMethods()", returning = "result")
    public void afterSuccess(JoinPoint joinPoint, Object result) {
        writeAudit(joinPoint, AuditResult.SUCCESS.name(), result, null);
    }

    /**
     * Advice that runs after a method annotated with @Audit throws an exception.
     *
     * @param joinPoint the join point representing the method call
     * @param ex        the exception thrown by the method
     */
    @AfterThrowing(pointcut = "auditedMethods()", throwing = "ex")
    public void afterException(JoinPoint joinPoint, Throwable ex) {
        writeAudit(joinPoint, AuditResult.FAILURE.name(), null, ex);
    }

    /**
     * Writes an audit record with the method details, outcome, result, and error.
     *
     * @param joinPoint the join point representing the method call
     * @param outcome   the outcome of the method execution (SUCCESS or FAILURE)
     * @param result    the result of the method execution, if any
     * @param error     the exception thrown by the method, if any
     */
    private void writeAudit(JoinPoint joinPoint, String outcome, Object result, Throwable error) {
        log.info("Audit: {} - {} - {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                outcome);
        String method = joinPoint.getSignature().toShortString();
        Map<String, Object> details = new HashMap<>();
        details.put("class", joinPoint.getTarget().getClass().getSimpleName());
        details.put("method", method);
        details.put("arguments", joinPoint.getArgs());
        details.put("outcome", outcome);
        if (result != null) {
            details.put("result", result);
        }
        if (error != null) {
            details.put("error", error.getMessage());
        }

        AuditDto audit = new AuditDto(
                null,
                method,
                Instant.now(),
                details
        );

        auditService.save(audit); // async
    }
}