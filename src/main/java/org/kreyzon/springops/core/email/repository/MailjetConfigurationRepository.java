package org.kreyzon.springops.core.email.repository;

import org.kreyzon.springops.core.email.entity.MailjetConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MailjetConfigurationRepository extends JpaRepository<MailjetConfiguration, UUID> {

}
