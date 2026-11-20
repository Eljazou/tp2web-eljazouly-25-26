package ma.emsi.eljazouly.tp2web.llm;

/**
 * Interface de l'assistant.
 * On n'écrit pas d'implémentation : c'est AiServices (LangChain4j) qui la génère.
 * Quand on appelle chat, LangChain4j ajoute la question à la mémoire, envoie au LLM
 * tous les messages de la mémoire (rôle système compris) et retourne la réponse du LLM.
 */
public interface Assistant {
    String chat(String prompt);
}
