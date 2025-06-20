package org.kreyzon.springops.email.repository;

import org.kreyzon.springops.email.entity.EmailConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmailConfigurationRepository extends JpaRepository<EmailConfiguration, UUID> {

}
