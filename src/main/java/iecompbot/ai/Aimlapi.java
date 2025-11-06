package iecompbot.ai;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static iecompbot.interaction.Automation.LogSlash;
import static my.utilities.json.JSONItem.GSON;
import static my.utilities.util.Utilities.StopString;

public class Aimlapi {
    public String apiKey; // Replace with your actual API key
    public String apiUrl = "https://api.aimlapi.com/v1/chat/completions";


    public Aimlapi(String apiKey) {
        this.apiKey = apiKey;
    }

    public String askAISimple(String message) throws IOException, InterruptedException {
        ChatRequest CR = new ChatRequest("gpt-4o", List.of(
                new ChatRequest.Message("system", "You are an AI assistant which is used to help provide information to users."),
                new ChatRequest.Message("user", message)));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(CR)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.err.println(response.body());
        ChatResponse chatResponse = GSON.fromJson(response.body(), ChatResponse.class);
        return chatResponse.choices.getFirst().message.content;
    }



    public String confirmData(String message) throws IOException, InterruptedException {
        message = message.replaceAll("  ", "").replaceAll("  ", "");
        ChatRequest CR = new ChatRequest("gpt-4o", List.of(
                new ChatRequest.Message("system", "You are an AI assistant which is used to confirm the request of a user on 3 lines." +
                        "The 1st line must be one of these words ALLTIME, ACTIVE, CLOSED. Which define which type of information is he asking for. Whether he is asking for something which is here all the time. Or something underway, open or active. Or something which already ended or is closed. Like tournaments, servers or events or clans can use this. Most of the time, will be all time for random requests." +
                        "The 2nd line must be one of these words USER, CLAN, SERVER, EVENT, TOURNAMENT, GAME. Each define the type of data the user is asking help. A user can be a profile, a person, a player or anything in priority. A clan can also be a team. A server can be a discord or community. An event could be a world cup, clan cup, euro, can championship. A game could be either asking information about what's better, the meta, strategy or how to play overall a game." +
                        "The 3rd line must be the name of the data the user is asking. For example if he ask about data of a person/user or a clan or event. It should be the person name. The word should at least be 3 of length. If the user ask data which is part of something, like for example ask if this organiser was part of this event. Give the event name instead. Always focus the parent of an object. Also if the user is talking about multiple names. Separate with a /." +
                        "Additionally if the message require none of this. You can just reply NONE. The user maybe just want to chat."),
                new ChatRequest.Message("user", message)));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(CR)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.err.println(response.body());
        FileWriter FW = new FileWriter("./OUTPUT.txt");
        PrintWriter PR = new PrintWriter(FW);
        PR.println(response.body());
        PR.close();
        FW.close();
        ChatResponse chatResponse = GSON.fromJson(response.body(), ChatResponse.class);
        return chatResponse.choices.getFirst().message.content.toLowerCase();
    }

    public String askAI(String message) throws Exception {
        return askAI(message, null);
    }
    public String askAI(String message, String sourcedata) throws Exception {
        ChatRequest CR = new ChatRequest("gpt-4o", List.of(
                new ChatRequest.Message("system", "You are an AI assistant named 'Inazuma Competitive' created by LoicMDF which is used to help provide information to users about Inazuma Eleven competitive clans, users, tournaments or events in a Discord chat. You can also answer requests of users on Discord." +
                        "Do not talking about things such as ids, image urls. And also don't provide each attribute in a list form, but part of a paragraph unless asked or if the data is appropriate for. Else it should be descriptive and paragraphic. Do not make a paragraph large, you can divide them in smaller 3-4 lines ones. If something is null or 0, don't mention or talk about it." +
                        "All data in epoch seconds or epoch millis should be converted as approppriate." +
                        "Also I want you to bold the Names of things using ** at the end and start of the name." +
                        "Some names such as clans and games have a discord-formatted emoji next to them in the data given. DO NOT REMOVE them when writing the names as these are for text decoration, example: '<emojiname:numbers> **object name**' (the <:emojiname:1234> is just an example, use the one given next to the object name)." +
                        "DO NOT include emojis in the bold quotes (**)." +
                        "You should properly write hyperlinks for discord chats it is written as (text)[link], you should not put the link as text." +
                        "Also do not write Discord server invite links as hyperlinks, just write the link as it is but you can use hyperlinks for other social media links." +
                        "Also never put dots (.) at the end of links if they are at the end of a sentense because they won't work anymore, put a space before the dot like this \"(link) .\"" +
                        "Always respond in the language the user is talking to you." +
                        "If the user insist to find something and you don't find it. Don't make things up and say you didn't find anything." +
                        "The user may not all the time ask for things, he may just want to have a conversation and you can talk to him normally while still giving him information when he asks." +
                        "Max length of your answer is 2000, not more. Never exceed this character limit." +
                        "Data: " + sourcedata),
                new ChatRequest.Message("user", message)));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(CR)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ChatResponse chatResponse = GSON.fromJson(response.body(), ChatResponse.class);
        System.err.println(response.body());
        String rep = chatResponse.choices.getFirst().message.content;
        System.out.println(rep.length() + ": " + rep);
        LogSlash("**[AI Response]** `Inazuma Competitive` : " + StopString(rep, 1900));
        return StopString(rep, 2000);
    }


    public static class ChatRequest {
        public String model;
        public List<Message> messages;

        public ChatRequest(String model, List<Message> messages) {
            this.model = model;
            this.messages = messages;
        }
        public static class Message {
            public String role;
            public String content;
            public Message(String role, String content) {
                this.role = role;
                this.content = content;
            }
        }
    }


    public static class ChatResponse {
        public String id;
        public String object;
        public long created;
        public String model;
        public List<Choice> choices;
        public Usage usage;
        public String system_fingerprint;

        public static class Choice {
            public int index;
            public String finish_reason;
            public ChatRequest.Message message;
        }

        public static class Usage {
            public int prompt_tokens;
            public int completion_tokens;
            public int total_tokens;
            public PromptTokensDetails prompt_tokens_details;
            public CompletionTokensDetails completion_tokens_details;
        }

        public static class PromptTokensDetails {
            public int cached_tokens;
            public int audio_tokens;
        }

        public static class CompletionTokensDetails {
            public int reasoning_tokens;
            public int audio_tokens;
            public int accepted_prediction_tokens;
            public int rejected_prediction_tokens;
        }
    }
}
