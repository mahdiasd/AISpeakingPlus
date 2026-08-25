package ir.speaking.core.network.model

import ir.speaking.feature.chat.model.Chat
import kotlinx.serialization.json.*

fun generateStreamGeminiRequest(chats: List<Chat>, systemPrompt: String): JsonObject {
    return buildJsonObject {

        putJsonArray("contents") {
            chats.forEach { chat ->
                addJsonObject {
                    put("role", chat.role.key)
                    putJsonArray("parts") {
                        addJsonObject {
                            put("text", chat.message.ifEmpty { "Start the conversation" })
                        }
                    }
                }
            }
        }

        putJsonObject("system_instruction") {
            putJsonArray("parts") {
                addJsonObject {
                    put("text", systemPrompt)
                }
            }
        }

        putJsonObject("generation_config") {
            put("temperature", 0.9)
            put("max_output_tokens", 200)

//            putJsonObject("response_schema") {
//                put("type", "object")
//                putJsonObject("properties") {
//                    putJsonObject("message") {
//                        put("type", "string")
//                    }
//                    putJsonObject("finishedTasksIndex") {
//                        put("type", "array")
//                        putJsonObject("items") {
//                            put("type", "integer")
//                        }
//                    }
//                    putJsonObject("grammar") {
//                        put("type", "string")
//                    }
//                }
//                putJsonArray("required") {
//                    add(JsonPrimitive("message"))
//                    add(JsonPrimitive("finishedTasksIndex"))
//                }
//            }
        }
    }
}


fun generateGeminiRequest(chats: List<Chat>, systemPrompt: String): JsonObject {
    return buildJsonObject {
        putJsonArray("contents") {
            chats.forEach { chat ->
                addJsonObject {
                    put("role", chat.role.key)
                    putJsonArray("parts") {
                        addJsonObject { put("text", chat.message) }
                    }
                }
            }
        }

        putJsonObject("system_instruction") {
            putJsonArray("parts") {
                addJsonObject { put("text", systemPrompt) }
            }
        }

        putJsonObject("generation_config") {
            put("temperature", 0.65)
            put("max_output_tokens", 500)
            put("response_mime_type", "application/json")

            putJsonObject("response_schema") {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("message") {
                        put("type", "string")
                        put("description", "In-character response")
                    }

                    putJsonObject("finishedTasksIndex") {
                        put("type", "array")
                        putJsonObject("items") {
                            put("type", "integer")
                        }
                    }

                    putJsonObject("grammar") {
                        put("type", "object")
                        put("description", "Check users latest message")
                        putJsonObject("properties") {
                            putJsonObject("status") {
                                put("type", "string")
                                putJsonArray("enum") {
                                    add(JsonPrimitive("Error"))
                                    add(JsonPrimitive("Ok"))
                                }
                            }
                            putJsonObject("message") {
                                put("type", "string")
                            }
                        }
                        putJsonArray("required") {
                            add("status")
                            add("message")
                        }
                    }

                    putJsonObject("translatedText") {
                        put("type", "string")
                    }

                    putJsonObject("suggests") {
                        put("type", "array")
                        putJsonObject("items") {
                            put("type", "string")
                        }
                    }
                }

                putJsonArray("required") {
                    add("message")
                    add("finishedTasksIndex")
                    add("translatedText")
                    add("suggests")
                    add("grammar")
                }
            }
        }
    }
}

fun generateGeminiChatRequest(chats: List<Chat>, systemPrompt: String): JsonObject {
    return buildJsonObject {
        putJsonArray("contents") {
            chats.forEach { chat ->
                addJsonObject {
                    put("role", chat.role.key)
                    putJsonArray("parts") {
                        addJsonObject { put("text", chat.message) }
                    }
                }
            }
        }

        putJsonObject("system_instruction") {
            putJsonArray("parts") {
                addJsonObject { put("text", systemPrompt) }
            }
        }

        putJsonObject("generation_config") {
            put("temperature", 0.65)
            put("max_output_tokens", 250)
            put("response_mime_type", "application/json")

            putJsonObject("response_schema") {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("message") {
                        put("type", "string")
                        put("description", "In-character response")
                    }
                    putJsonObject("finishedTasksIndex") {
                        put("type", "array")
                        putJsonObject("items") {
                            put("type", "integer")
                        }
                    }
                    putJsonObject("grammar") {
                        put("type", "object")
                        put("description", "Check users latest message")
                        putJsonObject("properties") {
                            putJsonObject("status") {
                                put("type", "string")
                                putJsonArray("enum") {
                                    // Gemini API expects lowercase for enums in schema
                                    add(JsonPrimitive("error"))
                                    add(JsonPrimitive("ok"))
                                }
                            }
                            putJsonObject("message") {
                                put("type", "string")
                            }
                        }
                        putJsonArray("required") {
                            add("status")
                            add("message")
                        }
                    }
                    putJsonObject("translatedText") {
                        put("type", "string")
                    }
                }
                putJsonArray("required") {
                    add("message")
                    add("finishedTasksIndex")
                    add("translatedText")
                    add("grammar")
                }
            }
        }
    }
}

fun generateSuggestionRequest(prompt: String): JsonObject {
    return buildJsonObject {
        putJsonArray("contents") {
            addJsonObject {
                put("role", "user")
                putJsonArray("parts") {
                    addJsonObject { put("text", prompt) }
                }
            }
        }

        putJsonObject("generation_config") {
            put("temperature", 0.7)
            put("max_output_tokens", 100)
            put("response_mime_type", "application/json")

            putJsonObject("response_schema") {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("suggests") {
                        put("type", "array")
                        putJsonObject("items") {
                            put("type", "string")
                        }
                    }
                }
                putJsonArray("required") {
                    add("suggests")
                }
            }
        }
    }
}

