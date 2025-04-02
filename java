import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication; import org.springframework.web.bind.annotation.; import org.springframework.web.client.RestTemplate; import java.util.;

@SpringBootApplication @RestController @RequestMapping("/numbers") public class AverageCalculatorService {

private static final int WINDOW_SIZE = 10;
private static final Map<String, Deque<Integer>> storage = new HashMap<>();
private static final Map<String, String> API_URLS = Map.of(
    "p", "https://thirdparty.com/prime",
    "f", "https://thirdparty.com/fibonacci",
    "e", "https://thirdparty.com/even",
    "r", "https://thirdparty.com/random"
);

public static void main(String[] args) {
    SpringApplication.run(AverageCalculatorService.class, args);
}

@GetMapping("/{numberid}")
public Map<String, Object> getNumbers(@PathVariable String numberid) {
    if (!API_URLS.containsKey(numberid)) {
        throw new IllegalArgumentException("Invalid number ID");
    }

    long startTime = System.currentTimeMillis();
    RestTemplate restTemplate = new RestTemplate();
    List<Integer> numbers = new ArrayList<>();

    try {
        Map<?, ?> response = restTemplate.getForObject(API_URLS.get(numberid), Map.class);
        numbers = (List<Integer>) response.get("numbers");
    } catch (Exception e) {
        numbers = new ArrayList<>();
    }

    storage.putIfAbsent(numberid, new ArrayDeque<>());
    Deque<Integer> window = storage.get(numberid);
    
    Set<Integer> uniqueNumbers = new HashSet<>(numbers);
    uniqueNumbers.removeAll(window);
    window.addAll(uniqueNumbers);

    while (window.size() > WINDOW_SIZE) {
        window.pollFirst();
    }

    double average = window.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    
    Map<String, Object> response = new HashMap<>();
    response.put("numbers", new ArrayList<>(window));
    response.put("average", average);
    response.put("response_time", System.currentTimeMillis() - startTime + "ms");
    return response;
}

}
