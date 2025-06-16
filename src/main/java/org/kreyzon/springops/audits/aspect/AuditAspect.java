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
 * This aspect intercepts methods explicitly annotated with {@link org.kreyzon.springops.config.annotations.Audit}
 * or {@link org.kreyzon.springops.config.annotations.SensibleAudit}.
 *
 * <p>Methods annotated with {@link org.kreyzon.springops.config.annotations.Audit} will have their audit records
 * written to the database with full details, while methods annotated with {@link org.kreyzon.springops.config.annotations.SensibleAudit}
 * will have their audit records saved without sensitive data.</p>
 *
 * <p>This class uses Spring AOP and AspectJ annotations to define pointcuts and advices for auditing.</p>
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
     * Pointcut that matches methods annotated with {@link org.kreyzon.springops.config.annotations.Audit}.
     */
    @Pointcut("@annotation(org.kreyzon.springops.config.annotations.Audit)")
    public void auditedMethods() {}

    /**
     * Pointcut that matches methods annotated with {@link org.kreyzon.springops.config.annotations.SensibleAudit}.
     */
    @Pointcut("@annotation(org.kreyzon.springops.config.annotations.SensibleAudit)")
    public void sensibleAuditedMethods() {}

    /**
     * Advice that runs after a method annotated with {@link org.kreyzon.springops.config.annotations.Audit}
     * is executed successfully. It writes an audit record with the method details and result.
     *
     * @param joinPoint the join point representing the method call
     * @param result    the result of the method execution
     */
    @AfterReturning(pointcut = "auditedMethods()", returning = "result")
    public void afterSuccess(JoinPoint joinPoint, Object result) {
        writeAudit(joinPoint, AuditResult.SUCCESS.name(), result, null, false);
    }

    /**
     * Advice that runs after a method annotated with {@link org.kreyzon.springops.config.annotations.Audit}
     * throws an exception. It writes an audit record with the method details and exception information.
     *
     * @param joinPoint the join point representing the method call
     * @param ex        the exception thrown by the method
     */
    @AfterThrowing(pointcut = "auditedMethods()", throwing = "ex")
    public void afterException(JoinPoint joinPoint, Throwable ex) {
        writeAudit(joinPoint, AuditResult.FAILURE.name(), null, ex, false);
    }

    /**
     * Advice that runs after a method annotated with {@link org.kreyzon.springops.config.annotations.SensibleAudit}
     * is executed successfully. It writes an audit record without sensitive data.
     *
     * @param joinPoint the join point representing the method call
     * @param result    the result of the method execution
     */
    @AfterReturning(pointcut = "sensibleAuditedMethods()", returning = "result")
    public void afterSensibleSuccess(JoinPoint joinPoint, Object result) {
        writeAudit(joinPoint, AuditResult.SUCCESS.name(), null, null, true);
    }

    /**
     * Advice that runs after a method annotated with {@link org.kreyzon.springops.config.annotations.SensibleAudit}
     * throws an exception. It writes an audit record without sensitive data.
     *
     * @param joinPoint the join point representing the method call
     * @param ex        the exception thrown by the method
     */
    @AfterThrowing(pointcut = "sensibleAuditedMethods()", throwing = "ex")
    public void afterSensibleException(JoinPoint joinPoint, Throwable ex) {
        writeAudit(joinPoint, AuditResult.FAILURE.name(), null, null, true);
    }

    /**
     * Writes an audit record with the method details, execution outcome, result, and error (if any).
     * If the method is annotated with {@link org.kreyzon.springops.config.annotations.SensibleAudit},
     * sensitive data (e.g., arguments, result, and error details) is excluded from the audit record.
     *
     * @param joinPoint the join point representing the method call
     * @param outcome   the outcome of the method execution (e.g., SUCCESS or FAILURE)
     * @param result    the result of the method execution, if any
     * @param error     the exception thrown by the method, if any
     * @param isSensible whether the method is annotated with {@link org.kreyzon.springops.config.annotations.SensibleAudit}
     */
    private void writeAudit(JoinPoint joinPoint, String outcome, Object result, Throwable error, boolean isSensible) {
        // Log the audit details
        log.info("Audit: {} - {} - {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                outcome);

        // Extract method details
        String method = joinPoint.getSignature().toShortString();
        Map<String, Object> details = new HashMap<>();
        details.put("class", joinPoint.getTarget().getClass().getSimpleName());
        details.put("method", method);

        // Add sensitive data only if not marked as sensitive
        if (!isSensible) {
            details.put("arguments", joinPoint.getArgs());
            details.put("outcome", outcome);
            if (result != null) {
                details.put("result", result);
            }
            if (error != null) {
                details.put("error", error.getMessage());
            }
        } else {
            details.put("outcome", "SENSIBLE AUDIT" + (error != null ? " - " + error.getMessage() : ""));
        }

        // Create an audit DTO
        AuditDto audit = new AuditDto(
                null,
                method,
                Instant.now(),
                details,
                "system" // Default user, can be overridden in AuditService
        );

        // Save the audit record asynchronously
        auditService.save(audit);
    }
}