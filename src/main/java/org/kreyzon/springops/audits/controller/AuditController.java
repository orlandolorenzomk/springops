package org.kreyzon.springops.audits.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.audits.entity.Audit;
import org.kreyzon.springops.audits.service.AuditService;
import org.kreyzon.springops.common.dto.audits.AuditDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * REST controller for managing audit operations.
 * Provides endpoints for retrieving and searching audits.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@RestController
@RequestMapping("/audits")
@RequiredArgsConstructor
@Slf4j
public class AuditController {

    private final AuditService auditService;

    /**
     * Retrieves an audit record by its ID.
     *
     * @param auditId the ID of the audit record to retrieve
     * @return the audit record as an AuditDto
     */
    @GetMapping("/{auditId}")
    public AuditDto findById(@PathVariable Integer auditId) {
        log.debug("Received request to retrieve audit with ID: {}", auditId);
        return auditService.findById(auditId);
    }

    /**
     * Searches for audits based on dynamic criteria with pagination.
     *
     * @param userId   the user ID to filter by (optional)
     * @param action   the action to filter by (optional)
     * @param from     the start timestamp to filter by (optional)
     * @param to       the end timestamp to filter by (optional)
     * @param page     the page number for pagination (default: 0)
     * @param size     the page size for pagination (default: 10)
     * @return a paginated list of audits matching the criteria
     */
    @GetMapping
    public Page<AuditDto> searchAudits(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Received request to search audits with criteria: userId={}, action={}, from={}, to={}, page={}, size={}",
                userId, action, from, to, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return auditService.searchAudits(userId, action, from, to, pageable);
    }
}