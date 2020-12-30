package jp.example.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.example.models.Employee;
import jp.example.models.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {
	public Page<Report> findByEmployee(Employee employee, Pageable pageable);
	public Long countByEmployee(Employee employee);
}

