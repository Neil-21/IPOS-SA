package com.infopharma.ipos_sa.repository;

/**
 * PUApplicationRepository
 * Spring Data JPA repository for {@link com.infopharma.ipos_sa.entity.PUApplication}.
 * Uses only inherited {@code JpaRepository} methods — applications are
 * retrieved by ID or as a full list; no date-range or status queries needed
 * as filtering is done in the controller layer.
 */
import com.infopharma.ipos_sa.entity.PUApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PUApplicationRepository extends JpaRepository<PUApplication, String> {
}
