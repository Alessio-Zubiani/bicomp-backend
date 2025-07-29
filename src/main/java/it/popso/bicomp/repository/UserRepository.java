package it.popso.bicomp.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.popso.bicomp.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, BigDecimal> {
	
	Optional<User> findByRegistrationNumber(String registrationNumber);
	
	@Query("SELECT u FROM User u ORDER BY u.registrationNumber ASC")
	List<User> findAll();
	
}
