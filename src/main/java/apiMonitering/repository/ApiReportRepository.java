package apiMonitering.repository;

import apiMonitering.domain.ApiReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiReportRepository extends JpaRepository<ApiReport, Long> {
}
