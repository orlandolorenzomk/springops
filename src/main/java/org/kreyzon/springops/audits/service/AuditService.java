package org.kreyzon.springops.audits.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.audits.entity.Audit;
import org.kreyzon.springops.audits.repository.AuditRepository;
import org.kreyzon.springops.auth.model.User;
import org.kreyzon.springops.auth.service.UserService;
import org.kreyzon.springops.auth.util.JwtUtil;
import org.kreyzon.springops.common.dto.audits.AuditDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;

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
        return attrs.getRequest();
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

        // authHeader is either present or null
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return "system";

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) return "invalid-token";

        return jwtUtil.extractUsername(token);
    }


    /**
     * Deletes all audit records that are older than one month.
     * This method retrieves audits older than one month and deletes them from the repository.
     */
    public void deleteAuditsOlderThanAMonth() {
        log.debug("Retrieving audits from the last month");
        List<Audit> audits = auditRepository.findOlderThanOneMonthAudits();
        if (audits.isEmpty()) {
            log.info("No audits found older than one month");
        } else {
            log.info("Found {} audits older than one month", audits.size());
        }
        auditRepository.deleteAll(audits);
    }

    /**
     * Searches for audits based on dynamic criteria with pagination.
     *
     * @param userId   the user ID to filter by (optional)
     * @param action   the action to filter by (optional)
     * @param from     the start timestamp to filter by (optional)
     * @param to       the end timestamp to filter by (optional)
     * @param pageable the pagination information
     * @return a paginated list of audits matching the criteria
     */
    public Page<AuditDto> searchAudits(Integer userId, String action, Instant from, Instant to, Pageable pageable) {
        Specification<Audit> spec = Audit.buildSpecification(userId, action, from, to);

        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "timestamp")
            );
        }

        log.debug("Searching audits with criteria: userId={}, action={}, from={}, to={}, pageable={}",
                userId, action, from, to, pageable);
        Page<Audit> auditPage = auditRepository.findAll(spec, pageable);
        log.debug("Found {} audits matching the criteria", auditPage.getTotalElements());
        return auditPage.map(AuditDto::fromEntity);
    }
}
