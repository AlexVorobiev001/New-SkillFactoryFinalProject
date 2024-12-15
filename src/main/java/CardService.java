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
        String sql = "UPDATE Card SET balance = balance + ? WHERE userId = ?";
        int rowsAffected = jdbcTemplate.update(sql, amount, userId);

        if (rowsAffected == 0) {
            return objectMapper.createObjectNode().put("error", "User not found");
        }

        return objectMapper.createObjectNode().put("message", "Balance updated successfully");

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

            String sql = "UPDATE Card SET balance = balance - ? WHERE userId = ?";
            int rowsAffected = jdbcTemplate.update(sql, amount, userId);

            if (rowsAffected == 0) {
                return objectMapper.createObjectNode().put("error", "Database error: Update failed");
            }

            return objectMapper.createObjectNode().put("message", "Money withdrawn successfully");
        } catch (EmptyResultDataAccessException e) {
            return objectMapper.createObjectNode().put("error", "User not found");
        } catch (DataAccessException e) {

            return objectMapper.createObjectNode().put("error", "Database error: " + e.getMessage());

        }
    }
}



