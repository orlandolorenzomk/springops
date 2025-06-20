package org.kreyzon.springops.audits.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kreyzon.springops.audits.entity.Audit;
import org.kreyzon.springops.audits.service.AuditService;
import org.kreyzon.springops.common.dto.audits.AuditDto;
import org.kreyzon.springops.common.dto.audits.AuditFilterDto;
import org.kreyzon.springops.common.dto.audits.AuditStatusDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

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
    public ResponseEntity<AuditDto> findById(@PathVariable Integer auditId) {
        log.debug("Received request to retrieve audit with ID: {}", auditId);
        AuditDto audit = auditService.findById(auditId);
        return ResponseEntity.ok(audit);
    }

    /**
     * Searches for audits based on the provided filter criteria.
     *
     * @param filterDto the filter criteria for searching audits
     * @param page      the page number to retrieve (default is 0)
     * @param size      the number of records per page (default is 10)
     * @return a paginated list of AuditDto matching the filter criteria
     */
    @PostMapping("/search")
    public ResponseEntity<Page<AuditDto>> searchAudits(
            @RequestBody AuditFilterDto filterDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditDto> audits = auditService.searchAudits(
                filterDto.getUserId(),
                filterDto.getAction(),
                filterDto.getFrom(),
                filterDto.getTo(),
                pageable
        );
        return ResponseEntity.ok(audits);
    }


    /**
     * Retrieves a list of audits older than one month.
     *
     * @return a list of AuditDto representing audits older than one month
     */
    @GetMapping("/statuses")
    public ResponseEntity<List<AuditStatusDto>> getAvailableStatuses() {
        log.debug("Received request to retrieve available audit statuses");
        List<AuditStatusDto> statuses = auditService.getUniqueAuditStatuses();
        return ResponseEntity.ok(statuses);
    }
}