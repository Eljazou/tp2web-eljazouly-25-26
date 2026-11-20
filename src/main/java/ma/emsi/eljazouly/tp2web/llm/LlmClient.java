package ma.emsi.eljazouly.tp2web.llm;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.Dependent;

import java.io.Serializable;

/**
 * Classe métier qui gère les échanges avec le LLM (Gemini), grâce à LangChain4j.
 * Le JSON n'apparaît plus nulle part : LangChain4j s'en occupe.
 *
 * De portée dependent : elle a la même durée de vie que le backing bean (portée view) dans lequel
 * elle est injectée. Une nouvelle conversation (nouvelle vue) a donc une nouvelle instance,
 * avec une mémoire vide.
 */
@Dependent
public class LlmClient implements Serializable {

    /**
     * Nom du modèle Gemini. L'énoncé indique gemini-1.5-flash, qui n'est plus disponible ;
     * on prend le même modèle qu'au TP 1.
     */
    private static final String NOM_MODELE = "gemini-3.5-flash-lite";

    /**
     * Rôle système choisi dans la liste déroulante de la page JSF.
     */
    private String systemRole;
    /**
     * L'assistant, dont l'implémentation est générée par AiServices.
     */
    private Assistant assistant;
    /**
     * Mémoire de la conversation (les 10 derniers messages).
     * Comme l'API du LLM est sans état, c'est elle qui permet de renvoyer l'historique à chaque requête.
     */
    private ChatMemory chatMemory;
    // Remarque : l'assistant et la mémoire ne sont pas Serializable. Problème seulement si le
    // serveur doit faire une passivation du backing bean (même remarque qu'au TP 1 pour le client REST).

    public LlmClient() {
        // Récupère la clé secrète pour travailler avec l'API du LLM, mise dans une variable d'environnement
        // du système d'exploitation (comme au TP 1).
        String key = System.getenv("GEMINI_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("La variable d'environnement GEMINI_KEY n'est pas définie.");
        }

        // Le modèle de chat (l'énoncé parle de ChatLanguageModel, renommé ChatModel depuis LangChain4j 1.0).
        ChatModel model = GoogleGenAiChatModel.builder()
                .apiKey(key)
                .modelName(NOM_MODELE)
                .temperature(0.7)
                .build();

        // Mémoire de 10 messages, puis l'assistant créé par AiServices
        // (chatLanguageModel(...) dans l'énoncé, renommé chatModel(...) depuis LangChain4j 1.0).
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .build();
    }

    public String getSystemRole() {
        return systemRole;
    }

    /**
     * Fixe le rôle système. La mémoire est d'abord vidée : le rôle système doit être le premier
     * message de la conversation, et un changement de rôle commence une nouvelle conversation.
     *
     * @param systemRole le rôle système.
     */
    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
        this.chatMemory.clear();
        this.chatMemory.add(SystemMessage.from(systemRole));
    }

    /**
     * Envoie la question au LLM, par l'intermédiaire de l'assistant.
     *
     * @param question la question de l'utilisateur.
     * @return la réponse du LLM.
     */
    public String envoyerQuestion(String question) {
        return this.assistant.chat(question);
    }
}
