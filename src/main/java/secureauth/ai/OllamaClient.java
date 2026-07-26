package secureauth.ai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OllamaClient {

    private static final String URL = "http://localhost:11434/api/chat";
    private static final String MODEL = "qwen2.5-coder:3b";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Envía un prompt al modelo usando formato chat (compatible con Continue).
     */
    public String generate(String prompt) {

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(message);

            Map<String, Object> body = new HashMap<>();
            body.put("model", MODEL);
            body.put("messages", messages);
            body.put("stream", false);
            body.put("temperature", 0.2);

            String json = mapper.writeValueAsString(body);

            Request request = new Request.Builder()
                    .url(URL)
                    .post(RequestBody.create(json, MediaType.get("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {

                if (!response.isSuccessful()) {
                    return "Error HTTP: " + response.code();
                }

                // Obtener el cuerpo de la respuesta
                var responseBodyObj = response.body();
                // Verificar si el cuerpo de la respuesta es nulo
                if (responseBodyObj == null) {
                    return "Respuesta vacía del servidor";
                }

                String responseBody = responseBodyObj.string();

                Map<String, Object> result = mapper.readValue(
                        responseBody,
                        new TypeReference<Map<String, Object>>() {
                }
                );

                Object messageObj = result.get("message");

                if (!(messageObj instanceof Map<?, ?> messageMap)) {
                    return "Respuesta inválida del modelo";
                }

                Object content = messageMap.get("content");

                return content != null
                        ? content.toString()
                        : "Respuesta vacía";

            }

        } catch (IOException e) {
            return "Error conectando con Ollama: " + e.getMessage();
        }
    }
}
