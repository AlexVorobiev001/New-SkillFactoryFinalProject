import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/card")
public class CardController {

    @Autowired
    private CardService cardService;
    private final ObjectMapper objectMapper = new ObjectMapper();


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

    public static void main(String[] args) {
    }
}