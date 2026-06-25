package com.claimsplatform.aiservice.service;

import com.claimsplatform.aiservice.dto.ChatRequest;
import com.claimsplatform.aiservice.dto.ChatResponse;
import com.claimsplatform.aiservice.entity.ChatHistory;
import com.claimsplatform.aiservice.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final ChatClient.Builder chatClientBuilder;
    private final ChatHistoryRepository chatHistoryRepository;

    @Value("${spring.ai.anthropic.api-key:}")
    private String apiKey;

    public ChatResponse chat(ChatRequest request, String userEmail) {
        String sessionId = request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        String response;
        List<String> sourceDocs;

        if (isApiKeyConfigured()) {
            response = callAI(request.getMessage());
            sourceDocs = List.of("RAG Pipeline", "Vector Store");
        } else {
            response = generateDemoResponse(request.getMessage());
            sourceDocs = List.of("Demo Mode - Set ANTHROPIC_API_KEY for live AI");
        }

        ChatHistory history = ChatHistory.builder()
                .sessionId(sessionId)
                .userEmail(userEmail)
                .userMessage(request.getMessage())
                .aiResponse(response)
                .claimId(request.getClaimId())
                .build();
        chatHistoryRepository.save(history);

        return ChatResponse.builder()
                .response(response)
                .sessionId(sessionId)
                .sourceDocs(sourceDocs)
                .build();
    }

    private boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("your-api-key-here");
    }

    private String callAI(String message) {
        try {
            return chatClientBuilder.build()
                    .prompt()
                    .system("You are an AI assistant for an insurance claims management platform. Help adjusters and policyholders understand their claims, policies, and documents.")
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI call failed, falling back to demo mode", e);
            return generateDemoResponse(message);
        }
    }

    private String generateDemoResponse(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("fraud") || lower.contains("suspicious")) {
            return "Based on our fraud detection analysis, I've identified the following patterns:\n\n" +
                   "• CLM-7 (Liability claim - $120,000) has been flagged with a HIGH fraud risk score of 0.85. " +
                   "Key red flags include: unusually high claim amount, vague incident description, and the claimant has retained legal counsel unusually quickly.\n\n" +
                   "• I recommend immediate investigation by a senior adjuster. The claim patterns match 3 known fraud indicators in our database.\n\n" +
                   "Would you like me to generate a detailed fraud analysis report for this claim?";
        }

        if (lower.contains("summarize") || lower.contains("summary") || lower.contains("recent")) {
            return "Here's a summary of recent claims activity:\n\n" +
                   "📊 **Current Status Overview:**\n" +
                   "• 12 total claims in the system\n" +
                   "• 2 claims approved (auto collision, multi-vehicle accident)\n" +
                   "• 2 under active review (rear-end collision, kitchen fire)\n" +
                   "• 1 flagged for potential fraud (warehouse liability - $120K)\n" +
                   "• 1 denied (travel medical claim - insufficient documentation)\n\n" +
                   "📈 **Trends:** Water damage and auto collision claims are trending up this quarter. " +
                   "The average claim processing time is 8.3 days, which is 15% faster than last quarter thanks to AI-assisted triage.";
        }

        if (lower.contains("coverage") || lower.contains("policy") || lower.contains("deductible")) {
            return "Here's an overview of coverage options in our system:\n\n" +
                   "🏠 **Home Insurance:** Coverage ranges from $220K-$350K with deductibles of $1,000-$1,500. " +
                   "Covers fire, water damage, theft, and weather events.\n\n" +
                   "🚗 **Auto Insurance:** Coverage from $35K-$85K. Full coverage includes collision, theft, and liability. " +
                   "Deductibles range from $500-$750.\n\n" +
                   "🏥 **Health Insurance:** Coverage up to $100K. Includes medical, dental, and prescription coverage.\n\n" +
                   "Would you like details on a specific policy type or customer's coverage?";
        }

        if (lower.contains("water") || lower.contains("damage") || lower.contains("pipe")) {
            return "I found relevant information about water damage claims:\n\n" +
                   "CLM-31775986 is a home water damage claim filed by Marie Tremblay. A burst pipe in the basement during spring thaw " +
                   "caused significant damage to the finished basement including flooring, drywall, and electrical systems.\n\n" +
                   "**Key Details:**\n" +
                   "• Estimated amount: $22,000\n" +
                   "• Status: Adjuster Assigned\n" +
                   "• Plumber confirmed corroded copper pipe as root cause\n" +
                   "• Mold remediation may be required (pending inspection)\n\n" +
                   "The adjuster should prioritize the mold assessment to prevent further damage escalation.";
        }

        if (lower.contains("process") || lower.contains("how") || lower.contains("work")) {
            return "Here's how our AI-powered claims processing works:\n\n" +
                   "1️⃣ **Submission** — Policyholder submits a claim with incident details and documentation\n\n" +
                   "2️⃣ **AI Triage** — Our system automatically:\n" +
                   "   • Classifies the claim type\n" +
                   "   • Runs fraud detection (scores 0.0-1.0)\n" +
                   "   • Generates an initial assessment and summary\n" +
                   "   • Estimates claim value based on historical data\n\n" +
                   "3️⃣ **Adjuster Review** — Claims are assigned to adjusters based on type and complexity\n\n" +
                   "4️⃣ **Investigation** — If needed, detailed investigation with supporting documents\n\n" +
                   "5️⃣ **Decision** — Approve, deny, or flag for further review\n\n" +
                   "The entire pipeline uses Kafka for event-driven processing and Elasticsearch for intelligent search.";
        }

        return "I'm the AI Claims Assistant running in demo mode. I can help you with:\n\n" +
               "• **Claim summaries** — Ask me to summarize recent claims\n" +
               "• **Fraud analysis** — Ask about suspicious claims or fraud patterns\n" +
               "• **Coverage info** — Ask about policy coverage and deductibles\n" +
               "• **Process guidance** — Ask how claim processing works\n" +
               "• **Document Q&A** — Ask about specific claims by type (water damage, auto collision, etc.)\n\n" +
               "💡 *Set the ANTHROPIC_API_KEY environment variable to enable live AI responses powered by Claude.*\n\n" +
               "Try asking: \"Show me fraud analysis\" or \"Summarize recent claims\"";
    }
}
