package org.kreyzon.springops.core.email.repository;

import org.kreyzon.springops.core.email.entity.EmailConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmailConfigurationRepository extends JpaRepository<EmailConfiguration, UUID> {

}
