package com.gois.study.bkjavagenai.config;

public final class PromptDefaults {

    public static final String DEFAULT_MESSAGE =
            "Sorry, I am not aware of this context. Please let me know if I can help you with any other requests.";

    public static final String SYSTEM_PROMPT =
            "Please limit responses to known facts. "
                    + "If you do not know the context, respond with: "
                    + "\"" + DEFAULT_MESSAGE + "\"";

    private PromptDefaults() {
    }

    public static boolean isDefaultMessage(String response) {
        return response != null && response.trim().equals(DEFAULT_MESSAGE);
    }
}
