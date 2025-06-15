package org.kreyzon.springops.core.email.repository;

import org.kreyzon.springops.core.email.entity.SmtpConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SmtpConfigurationRepository extends JpaRepository<SmtpConfiguration, UUID> {
}
