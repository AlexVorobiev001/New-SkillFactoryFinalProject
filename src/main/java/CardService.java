import com.fasterxml.jackson.core.TreeCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class CardService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();



    public JsonNode getBalance(int userId) throws IOException {
        String sql = "SELECT balance FROM Card WHERE userId = ?";
        Double balance = jdbcTemplate.queryForObject(sql, Double.class, userId);
        if (balance == null) {
            return objectMapper.createObjectNode().put("error", "User not found");
        }
        return objectMapper.createObjectNode().put("balance", balance);
    }

    public JsonNode putMoney(int userId, double amount) throws IOException {
        try {
            Double currentBalance = jdbcTemplate.queryForObject("SELECT balance FROM Card WHERE userId = ?", Double.class, userId);

            if (currentBalance == null) {
                return objectMapper.createObjectNode().put("error", "User not found");
            }

            int rowsAffected = jdbcTemplate.update("UPDATE Card SET balance = balance + ? WHERE userId = ?", amount, userId);
            if (rowsAffected == 0) {
                return objectMapper.createObjectNode().put("error", "Database error: Update failed");
            }

            int operationType = 1;
            LocalDateTime now = LocalDateTime.now();
            jdbcTemplate.update("INSERT INTO Operations (user_id, operation_type, amount, operation_date) VALUES (?, ?, ?, ?)", userId, operationType, (int) amount, now);

            return objectMapper.createObjectNode().put("message", "Balance updated successfully");
        } catch (DataAccessException e) {
            return objectMapper.createObjectNode().put("error", "Database error: " + e.getMessage());
        }
    }

    public JsonNode takeMoney(int userId, double amount) throws IOException {
        try {
            Double currentBalance = jdbcTemplate.queryForObject("SELECT balance FROM Card WHERE userId = ?", Double.class, userId);

            if (currentBalance == null) {
                return objectMapper.createObjectNode().put("error", "User not found");
            }

            if (currentBalance < amount) {
                return objectMapper.createObjectNode().put("error", "Insufficient funds");
            }

            int rowsAffected = jdbcTemplate.update("UPDATE Card SET balance = balance - ? WHERE userId = ?", amount, userId);
            if (rowsAffected == 0) {
                return objectMapper.createObjectNode().put("error", "Database error: Update failed");
            }

            int operationType = 2;
            LocalDateTime now = LocalDateTime.now();
            jdbcTemplate.update("INSERT INTO Operations (user_id, operation_type, amount, operation_date) VALUES (?, ?, ?, ?)", userId, operationType, (int) amount, now);

            return objectMapper.createObjectNode().put("message", "Money withdrawn successfully");
        } catch (DataAccessException e) {
            return objectMapper.createObjectNode().put("error", "Database error: " + e.getMessage());
        }
    }

    public JsonNode getOperationList(int userId, LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        try {
            String query = "SELECT operation_date, operation_type, amount FROM Operations WHERE user_id = ?";
            if (startDate != null) {
                query += " AND operation_date >= ?";
            }
            if (endDate != null) {
                query += " AND operation_date <= ?";
            }
            query += " ORDER BY operation_date;";

            List<Map<String, Object>> results;
            if (startDate == null && endDate == null) {
                results = jdbcTemplate.queryForList(query, userId);
            } else if (endDate == null) {
                results = jdbcTemplate.queryForList(query, userId, startDate);

            } else if (startDate == null) {
                results = jdbcTemplate.queryForList(query, userId, endDate);
            } else {
                results = jdbcTemplate.queryForList(query, userId, startDate, endDate);
            }

            if (results.isEmpty()) {
                return objectMapper.createObjectNode().put("error", "No operations found for this user during this period.");
            }

            JsonNode operationsNode = objectMapper.valueToTree(results);
            return operationsNode;
        } catch (EmptyResultDataAccessException e) {
            return objectMapper.createObjectNode().put("error", "No operations found for this user during this period.");
        } catch (DataAccessException e) {
            return objectMapper.createObjectNode().put("error", "Database error: " + e.getMessage());
        }
    }
}




