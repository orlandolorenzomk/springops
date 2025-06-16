package org.kreyzon.springops.audits.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.audits.repository.AuditRepository;
import org.kreyzon.springops.auth.model.User;
import org.kreyzon.springops.auth.service.UserService;
import org.kreyzon.springops.auth.util.JwtUtil;
import org.kreyzon.springops.common.dto.audits.AuditDto;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Service class for managing audit operations.
 * This class is responsible for handling audit-related functionalities
 * such as saving and retrieving audit records.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    private HttpServletRequest getCurrentRequest() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * Retrieves an audit record by its ID.
     *
     * @param auditId the ID of the audit record to retrieve
     * @return the audit record as an AuditDto
     * @throws IllegalArgumentException if no audit is found with the given ID
     */
    public AuditDto findById(Integer auditId) {
        log.debug("Retrieving audit with ID: {}", auditId);
        return auditRepository.findById(auditId)
                .map(AuditDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Audit not found with ID: " + auditId));
    }

    /**
     * Saves an audit record to the database.
     *
     * @param auditDto the audit record to save
     * @return the saved audit record
     */
    public AuditDto save(AuditDto auditDto) {
        log.debug("Saving audit: {}", auditDto);
        var entity = AuditDto.toEntity(auditDto);

        try {
            String username = extractUsernameFromRequest();
            User user = userService.findByEmail(username);
            entity.setUser(user);
        } catch (Exception e) {
            log.warn("Unable to find user for audit entry: {}", e.getMessage());
        }

        var savedEntity = auditRepository.save(entity);
        return AuditDto.fromEntity(savedEntity);
    }

    /**
     * Extracts the username from the current HTTP request.
     *
     * @return the username extracted from the request, or "system" if no token is present
     */
    private String extractUsernameFromRequest() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) return "system";

        // authHeader is either present or null
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return "system";

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return "invalid-token";

        return jwtUtil.extractUsername(token);
    }
}
