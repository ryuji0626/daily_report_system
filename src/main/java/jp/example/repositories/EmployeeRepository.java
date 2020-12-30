package jp.example.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.example.models.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
	public Long countByCodeEquals(String code);
	public Employee findByDeleteFlagAndCodeAndPassword(Integer deleteFlag, String code, String password);
}

