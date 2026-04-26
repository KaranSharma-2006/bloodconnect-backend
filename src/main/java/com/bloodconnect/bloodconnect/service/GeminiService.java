package com.bloodconnect.bloodconnect.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();


    @jakarta.annotation.PostConstruct
    public void init() {
        System.out.println("🔧 Initializing OpenRouter AI...");
        System.out.println("📌 API Key starts with: " + (apiKey != null && apiKey.length() > 6 ? apiKey.substring(0, 6) : "INVALID"));
        System.out.println("📌 API URL: " + apiUrl);
        System.out.println("📌 Model: " + model);
    }

    public String getChatResponse(String userMessage) {
        try {
            System.out.println("📤 Sending message to OpenRouter AI: " + userMessage);
            
            String url = apiUrl + "/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            String systemPrompt = "You are a helpful blood donation assistant for Blood Connect. " +
                    "Help users with blood donation info. Keep responses concise (1-2 sentences).";
            
            // Build request body for OpenRouter API (OpenAI-compatible format)
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);

            List<Map<String, Object>> messages = new java.util.ArrayList<>();
            messages.add(systemMessage);
            messages.add(userMsg);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 500);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("🌐 Calling OpenRouter endpoint");
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getBody() != null) {
                System.out.println("✅ Got response from OpenRouter API");
                Map<String, Object> body = response.getBody();
                
                if (body.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> firstChoice = choices.get(0);
                        if (firstChoice.containsKey("message")) {
                            Map<String, Object> messageObj = (Map<String, Object>) firstChoice.get("message");
                            if (messageObj.containsKey("content")) {
                                String text = (String) messageObj.get("content");
                                if (text != null && !text.trim().isEmpty()) {
                                    System.out.println("✅ OpenRouter AI returned: " + text.substring(0, Math.min(50, text.length())));
                                    return text;
                                }
                            }
                        }
                    }
                }
            }
        } catch (HttpClientErrorException e) {
            System.out.println("❌ HTTP Error " + e.getStatusCode() + ": " + e.getMessage());
            System.out.println("Response: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("❌ Error calling OpenRouter API: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        System.out.println("⚠️ Using fallback response instead of AI");
        return provideFallbackResponse(userMessage);
    }

    private String provideFallbackResponse(String userMessage) {
        String msg = userMessage.toLowerCase().trim();
        
        // Detect specific queries
        if ((msg.contains("age") || msg.contains("old") || msg.contains("minimum")) && msg.contains("donate")) {
            return "You typically need to be at least 17 years old (16 with parental consent in some areas) and usually under 65-70 years old. Your blood bank can confirm specific age requirements.";
        } 
        if (msg.contains("can") && msg.contains("donate") || msg.contains("able")) {
            return "To donate blood, you must generally be 17+, weigh at least 110 lbs, be in good health, and not have certain medical conditions or take specific medications. Check your local blood bank's requirements.";
        }
        if (msg.contains("donate") || msg.contains("donation")) {
            return "Blood donation process: 1) Find a donation center 2) Check eligibility 3) Bring ID & proof of address 4) Complete health form 5) Donation takes 30-45 min. 🩸 Blood Connect helps hospitals find donors like you!";
        }
        if (msg.contains("blood") && (msg.contains("type") || msg.contains("group"))) {
            return "Main blood types are O+, O-, A+, A-, B+, B-, AB+, AB- (determined by proteins on red cells). O+ is most common, AB- is rarest. Blood Connect helps match donors to hospitals needing specific types.";
        }
        if (msg.contains("where") || msg.contains("find") || msg.contains("location") || msg.contains("bank")) {
            return "Blood Connect helps you locate nearby blood banks, donation centers, and hospitals. Use our search feature to filter by location and blood type. You can also see where donations are urgently needed!";
        }
        if (msg.contains("requirements") || msg.contains("eligible")) {
            return "Basic requirements: Age 17+ (16 with consent), weight 110+ lbs, good general health, no active infections, not on certain medications. Permanent disqualifications include certain diseases. Ask your blood bank about specific criteria.";
        }
        if (msg.contains("hi") || msg.contains("hello") || msg.contains("help") || msg.contains("what")) {
            return "Hi! I'm the Blood Connect AI assistant. Ask me about: donation eligibility & age requirements, blood types, finding donors/banks, or donation process. How can I help?";
        }
        
        return "I can help with blood donation questions! Ask about age limits, eligibility, blood types, or finding donation centers. 🩸";
    }
}
