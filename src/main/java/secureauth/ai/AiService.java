package secureauth.ai;

public class AiService {

    private final OllamaClient client;

    public AiService(OllamaClient client) {
        this.client = client;
    }

    public String ask(String prompt) {
        return client.generate(prompt);
    }

    public String generateCode(String instruction) {
        return client.generate(
                "Eres un generador de código Java experto. " +
                "Devuelve solo código limpio.\n\n" +
                instruction
        );
    }
}