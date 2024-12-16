import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import java.io.IOException;

@RestController
@RequestMapping("/api/card")
public class CardController {

    @Autowired
    private CardService cardService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private JdbcOperations jdbcTemplate;


    @GetMapping("/{userId}/balance")
    public ResponseEntity<JsonNode> getBalance(@PathVariable int userId) throws IOException {
        JsonNode response = cardService.getBalance(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{userId}/balance")
    public ResponseEntity<JsonNode> putMoney(@PathVariable int userId, @RequestBody JsonNode requestBody) throws IOException {
        try {
            double amount = requestBody.get("amount").asDouble();
            JsonNode response = cardService.putMoney(userId, amount);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(objectMapper.createObjectNode().put("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<JsonNode> takeMoney(@PathVariable int userId, @RequestBody JsonNode requestBody) {
        try {

            double amount = requestBody.get("amount").asDouble();
            JsonNode response = cardService.takeMoney(userId, amount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return new ResponseEntity<>(objectMapper.createObjectNode().put("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{userId}/operations")
    public ResponseEntity<JsonNode> getOperationList(@PathVariable int userId,
                                                     @RequestParam(value = "startDate", required = false) String startDateStr,
                                                     @RequestParam(value = "endDate", required = false) String endDateStr) {

        try {
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;

            if (startDateStr != null) {
                startDate = LocalDateTime.parse(startDateStr, DateTimeFormatter.ISO_DATE_TIME);
            }
            if (endDateStr != null) {
                endDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ISO_DATE_TIME);
            }

            String query = "SELECT operation_date, operation_type, amount FROM Operations WHERE user_id = ? ";
            if(startDate != null){
                query += " AND operation_date >= ?";
            }
            if(endDate != null){
                query += " AND operation_date <= ?";
            }
            query += " ORDER BY operation_date;";


            List<Map<String, Object>> results;
            if(startDate == null && endDate == null){
                results = jdbcTemplate.queryForList(query, userId);
            }else if(endDate == null){
                results = jdbcTemplate.queryForList(query, userId,startDate);
            }else if(startDate == null){
                results = jdbcTemplate.queryForList(query, userId,endDate);
            }
            else{
                results = jdbcTemplate.queryForList(query, userId, startDate, endDate);
            }


            JsonNode responseNode = objectMapper.createObjectNode();
            ((ObjectNode) responseNode).putPOJO("operations", results);

            return new ResponseEntity<>(responseNode, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(objectMapper.createObjectNode().put("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    public static void main(String[] args) {
    }
}