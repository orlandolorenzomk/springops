package org.kreyzon.springops.audits.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.audits.entity.Audit;
import org.kreyzon.springops.audits.repository.AuditRepository;
import org.kreyzon.springops.auth.model.User;
import org.kreyzon.springops.auth.service.UserService;
import org.kreyzon.springops.auth.util.JwtUtil;
import org.kreyzon.springops.common.dto.audits.AuditDto;
import org.kreyzon.springops.common.dto.audits.AuditStatusDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
        try {
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
        } catch (Exception e) {
            log.error("Failed to save audit entry: {}", e.getMessage());
            return auditDto;
        }
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
     * Deletes all audit records that are older than the specified number of months.
     *
     * @param months number of months to use as threshold for deletion
     */
    @Transactional
    public void deleteAuditsOlderThanNMonths(Integer months) {
        log.info("Deleting audits older than {} month(s)", months);
        auditRepository.deleteOlderThanNMonths(months);
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
    public Page<AuditDto> searchAudits(UUID userId, String action, Instant from, Instant to, Pageable pageable) {
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

    /**
     * Retrieves a list of unique audit statuses from the database.
     *
     * @return a list of unique audit action strings
     */
    @Cacheable("uniqueAuditStatuses")
    public List<AuditStatusDto> getUniqueAuditStatuses() {
        log.debug("Fetching unique audit statuses from database");
        return auditRepository.findDistinctActions().stream()
                .map(status -> new AuditStatusDto(status, toHumanReadable(status)))
                .toList();
    }

    /**
     * Converts a raw audit action string to a human-readable format.
     * This method formats the action string by removing unnecessary parts
     * and converting it to a more readable form.
     *
     * @param raw the raw audit action string
     * @return a human-readable version of the audit action
     */
    private String toHumanReadable(String raw) {
        if (raw == null || !raw.contains(".")) return raw;

        String cleaned = raw.replace("(..)", "");
        String[] parts = cleaned.split("\\.");

        String servicePart = parts[0].replace("Service", "");
        String methodPart = parts[1]
                .replaceAll("([a-z])([A-Z])", "$1 $2") // camelCase → space separated
                .toLowerCase();

        return capitalize(servicePart) + " - " + capitalize(methodPart);
    }

    /**
     * Capitalizes the first letter of a given string.
     * If the input is null or empty, it returns the input as is.
     *
     * @param input the string to capitalize
     * @return the input string with the first letter capitalized
     */
    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * Evicts the cache for unique audit statuses.
     * This method is scheduled to run every 5 minutes to clear the cache.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // every 5 minutes
    @CacheEvict(value = "uniqueAuditStatuses", allEntries = true)
    public void evictUniqueAuditStatusesCache() {
        log.debug("Evicting unique audit statuses cache");
    }
}
