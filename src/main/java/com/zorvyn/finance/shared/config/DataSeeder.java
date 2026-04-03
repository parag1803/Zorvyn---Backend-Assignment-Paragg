package com.zorvyn.finance.shared.config;

import com.zorvyn.finance.finance.domain.FinancialRecord;
import com.zorvyn.finance.finance.domain.RecordCategory;
import com.zorvyn.finance.finance.domain.RecordType;
import com.zorvyn.finance.finance.repository.FinancialRecordRepository;
import com.zorvyn.finance.iam.domain.Role;
import com.zorvyn.finance.iam.domain.User;
import com.zorvyn.finance.iam.domain.UserStatus;
import com.zorvyn.finance.iam.repository.RoleRepository;
import com.zorvyn.finance.iam.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds demo users and sample financial records on first startup.
 * Uses the configured PasswordEncoder (BCrypt 12) to ensure correct hashing.
 * Only runs if no users exist in the database (idempotent).
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                                RoleRepository roleRepository,
                                FinancialRecordRepository recordRepository,
                                PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("Database already seeded — skipping");
                return;
            }

            log.info("Seeding demo data...");

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
            Role analystRole = roleRepository.findByName("ANALYST")
                    .orElseThrow(() -> new RuntimeException("ANALYST role not found"));
            Role viewerRole = roleRepository.findByName("VIEWER")
                    .orElseThrow(() -> new RuntimeException("VIEWER role not found"));

            // ── Create demo users ─────────────────────────────────────────
            User admin = createUser("admin@zorvyn.com", "Admin@123", "System", "Admin", adminRole, passwordEncoder);
            User analyst = createUser("analyst@zorvyn.com", "Analyst@123", "Jane", "Analyst", analystRole, passwordEncoder);
            User viewer = createUser("viewer@zorvyn.com", "Viewer@123", "John", "Viewer", viewerRole, passwordEncoder);

            admin = userRepository.save(admin);
            userRepository.save(analyst);
            userRepository.save(viewer);

            log.info("Created 3 demo users: admin@zorvyn.com, analyst@zorvyn.com, viewer@zorvyn.com");

            // ── Create sample financial records ───────────────────────────
            createSampleRecords(recordRepository, admin);
            log.info("Created 15 sample financial records");
            log.info("Seeding complete!");
        };
    }

    private User createUser(String email, String password, String firstName, String lastName,
                             Role role, PasswordEncoder encoder) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private void createSampleRecords(FinancialRecordRepository repo, User admin) {
        Object[][] records = {
            {75000, "INCOME", "SALARY", "Monthly salary - January", "2026-01-15"},
            {75000, "INCOME", "SALARY", "Monthly salary - February", "2026-02-15"},
            {75000, "INCOME", "SALARY", "Monthly salary - March", "2026-03-15"},
            {15000, "INCOME", "FREELANCE", "Freelance web development project", "2026-01-25"},
            {5000, "INCOME", "INVESTMENT", "Dividend from mutual funds", "2026-02-10"},
            {25000, "EXPENSE", "HOUSING", "Monthly rent - January", "2026-01-01"},
            {25000, "EXPENSE", "HOUSING", "Monthly rent - February", "2026-02-01"},
            {25000, "EXPENSE", "HOUSING", "Monthly rent - March", "2026-03-01"},
            {8000, "EXPENSE", "FOOD", "Monthly groceries - January", "2026-01-05"},
            {7500, "EXPENSE", "FOOD", "Monthly groceries - February", "2026-02-05"},
            {3000, "EXPENSE", "TRANSPORT", "Fuel and parking", "2026-01-10"},
            {2000, "EXPENSE", "UTILITIES", "Electricity bill", "2026-01-20"},
            {1500, "EXPENSE", "ENTERTAINMENT", "Movie night and dining", "2026-02-14"},
            {5000, "EXPENSE", "HEALTHCARE", "Annual health checkup", "2026-03-01"},
            {12000, "EXPENSE", "EDUCATION", "Online course subscription", "2026-01-08"},
        };

        for (Object[] r : records) {
            FinancialRecord rec = new FinancialRecord();
            rec.setUserId(admin.getId());
            rec.setAmount(BigDecimal.valueOf((int) r[0]));
            rec.setType(RecordType.valueOf((String) r[1]));
            rec.setCategory(RecordCategory.valueOf((String) r[2]));
            rec.setDescription((String) r[3]);
            rec.setTransactionDate(LocalDate.parse((String) r[4]));
            repo.save(rec);
        }
    }
}
