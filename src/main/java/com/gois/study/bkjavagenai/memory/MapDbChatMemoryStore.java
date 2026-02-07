package com.gois.study.bkjavagenai.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;
import static org.mapdb.Serializer.STRING;

public class MapDbChatMemoryStore implements ChatMemoryStore {

    private final DB db;
    private final Map<String, String> map;

    public MapDbChatMemoryStore(String fileName) {
        this.db = DBMaker.fileDB(fileName).transactionEnable().make();
        this.map = db.hashMap("messages", STRING, STRING).createOrOpen();
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = map.get(String.valueOf(memoryId));
        if (json == null || json.isBlank()) {
            return List.of();
        }
        return messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = messagesToJson(messages);
        map.put(String.valueOf(memoryId), json);
        db.commit();
    }

    @Override
    public void deleteMessages(Object memoryId) {
        map.remove(String.valueOf(memoryId));
        db.commit();
    }
}
