package org.kreyzon.springops.email.repository;

import org.kreyzon.springops.email.entity.MailjetConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MailjetConfigurationRepository extends JpaRepository<MailjetConfiguration, UUID> {

}
