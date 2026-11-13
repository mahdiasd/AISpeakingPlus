package ir.speaking.feature.placement.db

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll

data class RawQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctOption: String,
    val level: String,
    val category: String = "grammar"
)

object PlacementSeedData {
    val questions: List<RawQuestion> = listOf(
        // ====================== A1 (20 Questions) ======================
        RawQuestion("pq_a1_01", "Hello! I _____ Sarah.", listOf("am", "is", "are", "be"), "am", "A1", "grammar"),
        RawQuestion("pq_a1_02", "They _____ from Canada.", listOf("is", "are", "am", "be"), "are", "A1", "grammar"),
        RawQuestion("pq_a1_03", "This is _____ book.", listOf("my", "me", "I", "mine"), "my", "A1", "grammar"),
        RawQuestion("pq_a1_04", "Where _____ you live?", listOf("do", "does", "are", "is"), "do", "A1", "grammar"),
        RawQuestion("pq_a1_05", "He _____ coffee every morning.", listOf("drink", "drinks", "drinking", "drank"), "drinks", "A1", "grammar"),
        RawQuestion("pq_a1_06", "She doesn't _____ meat.", listOf("eats", "eat", "eating", "ate"), "eat", "A1", "grammar"),
        RawQuestion("pq_a1_07", "There _____ two apples on the table.", listOf("is", "are", "be", "was"), "are", "A1", "grammar"),
        RawQuestion("pq_a1_08", "What time _____ it?", listOf("is", "are", "does", "has"), "is", "A1", "grammar"),
        RawQuestion("pq_a1_09", "We go to work _____ bus.", listOf("by", "on", "in", "with"), "by", "A1", "vocabulary"),
        RawQuestion("pq_a1_10", "She has two _____.", listOf("childs", "children", "child", "childrens"), "children", "A1", "vocabulary"),
        RawQuestion("pq_a1_11", "Look at _____ birds in the sky.", listOf("those", "this", "that", "them"), "those", "A1", "grammar"),
        RawQuestion("pq_a1_12", "Can you _____ English?", listOf("speak", "speaks", "speaking", "spoke"), "speak", "A1", "grammar"),
        RawQuestion("pq_a1_13", "My sister is _____ teacher.", listOf("a", "an", "the", "one"), "a", "A1", "grammar"),
        RawQuestion("pq_a1_14", "I have breakfast _____ 8:00 AM.", listOf("at", "on", "in", "for"), "at", "A1", "grammar"),
        RawQuestion("pq_a1_15", "Jack isn't here. He is _____ home.", listOf("at", "to", "in", "on"), "at", "A1", "grammar"),
        RawQuestion("pq_a1_16", "Do you like _____ football?", listOf("playing", "play", "played", "plays"), "playing", "A1", "grammar"),
        RawQuestion("pq_a1_17", "Today is Monday, so tomorrow is _____.", listOf("Tuesday", "Wednesday", "Sunday", "Thursday"), "Tuesday", "A1", "vocabulary"),
        RawQuestion("pq_a1_18", "_____ is your name?", listOf("What", "Who", "Where", "How"), "What", "A1", "grammar"),
        RawQuestion("pq_a1_19", "I don't have _____ money with me.", listOf("any", "some", "no", "many"), "any", "A1", "grammar"),
        RawQuestion("pq_a1_20", "The cat is sleeping _____ the chair.", listOf("under", "between", "among", "into"), "under", "A1", "vocabulary"),

        // ====================== A2 (20 Questions) ======================
        RawQuestion("pq_a2_01", "Yesterday we _____ to the cinema.", listOf("went", "go", "gone", "goes"), "went", "A2", "grammar"),
        RawQuestion("pq_a2_02", "She didn't _____ her homework yesterday.", listOf("do", "did", "done", "does"), "do", "A2", "grammar"),
        RawQuestion("pq_a2_03", "An elephant is _____ than a horse.", listOf("bigger", "more big", "biggest", "bigness"), "bigger", "A2", "grammar"),
        RawQuestion("pq_a2_04", "This is the _____ movie I have ever seen.", listOf("best", "good", "better", "well"), "best", "A2", "grammar"),
        RawQuestion("pq_a2_05", "What are you _____ this weekend?", listOf("going to do", "go to do", "will to do", "doing to"), "going to do", "A2", "grammar"),
        RawQuestion("pq_a2_06", "I was cooking dinner when the phone _____.", listOf("rang", "rings", "was ringing", "rung"), "rang", "A2", "grammar"),
        RawQuestion("pq_a2_07", "You _____ wear a coat; it's very cold outside.", listOf("should", "might", "can", "may"), "should", "A2", "grammar"),
        RawQuestion("pq_a2_08", "Have you ever _____ to London?", listOf("been", "went", "go", "being"), "been", "A2", "grammar"),
        RawQuestion("pq_a2_09", "I haven't finished my homework _____.", listOf("yet", "already", "still", "just"), "yet", "A2", "grammar"),
        RawQuestion("pq_a2_10", "She enjoys _____ books in her free time.", listOf("reading", "to read", "read", "reads"), "reading", "A2", "grammar"),
        RawQuestion("pq_a2_11", "Could you please _____ me a favor?", listOf("do", "make", "give", "take"), "do", "A2", "vocabulary"),
        RawQuestion("pq_a2_12", "There is _____ milk left in the fridge. We need to buy some.", listOf("little", "few", "a few", "many"), "little", "A2", "grammar"),
        RawQuestion("pq_a2_13", "He is very interested _____ modern art.", listOf("in", "at", "about", "for"), "in", "A2", "vocabulary"),
        RawQuestion("pq_a2_14", "If it rains tomorrow, we _____ stay at home.", listOf("will", "would", "are", "were"), "will", "A2", "grammar"),
        RawQuestion("pq_a2_15", "How _____ does this shirt cost?", listOf("much", "many", "price", "long"), "much", "A2", "grammar"),
        RawQuestion("pq_a2_16", "The train arrives _____ London at 6 PM.", listOf("in", "at", "to", "on"), "in", "A2", "vocabulary"),
        RawQuestion("pq_a2_17", "We decided _____ a new car.", listOf("to buy", "buying", "buy", "bought"), "to buy", "A2", "grammar"),
        RawQuestion("pq_a2_18", "I was born _____ 1995.", listOf("in", "on", "at", "since"), "in", "A2", "grammar"),
        RawQuestion("pq_a2_19", "You _____ smoke here; it's strictly prohibited.", listOf("mustn't", "don't have to", "needn't", "shouldn't"), "mustn't", "A2", "grammar"),
        RawQuestion("pq_a2_20", "She speaks English very _____.", listOf("well", "good", "fluent", "nice"), "well", "A2", "vocabulary"),

        // ====================== B1 (20 Questions) ======================
        RawQuestion("pq_b1_01", "I _____ in this city for over five years now.", listOf("have lived", "lived", "am living", "was living"), "have lived", "B1", "grammar"),
        RawQuestion("pq_b1_02", "If I had more money, I _____ travel around the world.", listOf("would", "will", "can", "shall"), "would", "B1", "grammar"),
        RawQuestion("pq_b1_03", "This bridge _____ built in 1890.", listOf("was", "is", "has been", "were"), "was", "B1", "grammar"),
        RawQuestion("pq_b1_04", "The man _____ lives next door is a doctor.", listOf("who", "which", "whom", "whose"), "who", "B1", "grammar"),
        RawQuestion("pq_b1_05", "I'm looking forward to _____ you again.", listOf("seeing", "see", "saw", "seen"), "seeing", "B1", "vocabulary"),
        RawQuestion("pq_b1_06", "You don't need to hurry; we _____ plenty of time.", listOf("have", "had", "are having", "were having"), "have", "B1", "grammar"),
        RawQuestion("pq_b1_07", "She asked me where I _____ from.", listOf("came", "come", "am coming", "have come"), "came", "B1", "grammar"),
        RawQuestion("pq_b1_08", "By the time we arrived, the movie _____ already started.", listOf("had", "has", "was", "would"), "had", "B1", "grammar"),
        RawQuestion("pq_b1_09", "He suggested _____ a taxi because of the heavy rain.", listOf("taking", "to take", "take", "took"), "taking", "B1", "grammar"),
        RawQuestion("pq_b1_10", "Could you turn _____ the radio? I'm trying to concentrate.", listOf("down", "off", "away", "out"), "down", "B1", "vocabulary"),
        RawQuestion("pq_b1_11", "I'm used to _____ up early on weekdays.", listOf("waking", "wake", "woke", "woken"), "waking", "B1", "grammar"),
        RawQuestion("pq_b1_12", "Neither Tom _____ his brother could attend the party.", listOf("nor", "or", "and", "but"), "nor", "B1", "grammar"),
        RawQuestion("pq_b1_13", "The flight was delayed _____ the thick fog.", listOf("due to", "because", "although", "despite"), "due to", "B1", "vocabulary"),
        RawQuestion("pq_b1_14", "He worked hard _____ he could pass the examination.", listOf("so that", "in order", "because of", "such that"), "so that", "B1", "grammar"),
        RawQuestion("pq_b1_15", "I wish I _____ speak French fluently.", listOf("could", "can", "will", "may"), "could", "B1", "grammar"),
        RawQuestion("pq_b1_16", "You _____ better see a doctor about that persistent cough.", listOf("had", "would", "should", "could"), "had", "B1", "grammar"),
        RawQuestion("pq_b1_17", "She managed to solve the problem _____ any assistance.", listOf("without", "against", "besides", "except"), "without", "B1", "vocabulary"),
        RawQuestion("pq_b1_18", "The hotel _____ we stayed was extremely comfortable.", listOf("where", "which", "that", "whose"), "where", "B1", "grammar"),
        RawQuestion("pq_b1_19", "I'll call you as soon as I _____ the airport.", listOf("reach", "will reach", "reached", "have reached"), "reach", "B1", "grammar"),
        RawQuestion("pq_b1_20", "He apologized _____ being so late for the meeting.", listOf("for", "to", "about", "with"), "for", "B1", "vocabulary"),

        // ====================== B2 (20 Questions) ======================
        RawQuestion("pq_b2_01", "If you had informed me earlier, I _____ you at the station.", listOf("would have met", "would meet", "will meet", "had met"), "would have met", "B2", "grammar"),
        RawQuestion("pq_b2_02", "It's high time you _____ looking for a steady job.", listOf("started", "start", "have started", "had started"), "started", "B2", "grammar"),
        RawQuestion("pq_b2_03", "Despite _____ hard, he didn't secure the promotion.", listOf("working", "he worked", "work", "worked"), "working", "B2", "grammar"),
        RawQuestion("pq_b2_04", "The criminal is believed _____ the country two days ago.", listOf("to have left", "to leave", "having left", "leaving"), "to have left", "B2", "grammar"),
        RawQuestion("pq_b2_05", "She has _____ wonderful ideas that everyone loves working with her.", listOf("such", "so", "very", "too"), "such", "B2", "grammar"),
        RawQuestion("pq_b2_06", "Hardly _____ entered the building when the fire alarm sounded.", listOf("had we", "we had", "did we", "we did"), "had we", "B2", "grammar"),
        RawQuestion("pq_b2_07", "I'd rather you _____ smoke inside the apartment.", listOf("didn't", "don't", "not", "won't"), "didn't", "B2", "grammar"),
        RawQuestion("pq_b2_08", "The company took _____ twenty new employees last month.", listOf("on", "up", "in", "over"), "on", "B2", "vocabulary"),
        RawQuestion("pq_b2_09", "He speaks with such confidence that he carried _____ the presentation effortlessly.", listOf("off", "out", "away", "through"), "off", "B2", "vocabulary"),
        RawQuestion("pq_b2_10", "Had I known about the cancellation, I _____ the trouble of driving there.", listOf("wouldn't have taken", "wouldn't take", "didn't take", "hadn't taken"), "wouldn't have taken", "B2", "grammar"),
        RawQuestion("pq_b2_11", "The new regulations will come _____ effect next Monday.", listOf("into", "to", "in", "on"), "into", "B2", "vocabulary"),
        RawQuestion("pq_b2_12", "She is accused _____ leaking confidential financial data.", listOf("of", "for", "with", "about"), "of", "B2", "vocabulary"),
        RawQuestion("pq_b2_13", "We must take into _____ all unforeseen variables before signing.", listOf("account", "mind", "view", "thought"), "account", "B2", "vocabulary"),
        RawQuestion("pq_b2_14", "Not only _____ the contract, but they also offered a substantial bonus.", listOf("did they sign", "they signed", "signed they", "they did sign"), "did they sign", "B2", "grammar"),
        RawQuestion("pq_b2_15", "The manager insisted that the report _____ submitted by noon.", listOf("be", "is", "was", "would be"), "be", "B2", "grammar"),
        RawQuestion("pq_b2_16", "You cannot park here _____ you have a special permit.", listOf("unless", "provided", "as long as", "if"), "unless", "B2", "grammar"),
        RawQuestion("pq_b2_17", "By next December, I _____ working here for a full decade.", listOf("will have been", "will be", "would have been", "have been"), "will have been", "B2", "grammar"),
        RawQuestion("pq_b2_18", "He took a detour in order to _____ the traffic congestion on the highway.", listOf("avoid", "prevent", "refuse", "deny"), "avoid", "B2", "vocabulary"),
        RawQuestion("pq_b2_19", "I'm having difficulty coming up _____ a practical solution.", listOf("with", "to", "for", "against"), "with", "B2", "vocabulary"),
        RawQuestion("pq_b2_20", "She was so immersed in her novel that she completely lost track _____ time.", listOf("of", "in", "with", "to"), "of", "B2", "vocabulary"),

        // ====================== C1 (20 Questions) ======================
        RawQuestion("pq_c1_01", "Seldom _____ such an exquisite architectural masterpiece.", listOf("have I witnessed", "I have witnessed", "did I witness", "I witnessed"), "have I witnessed", "C1", "grammar"),
        RawQuestion("pq_c1_02", "Under no circumstances _____ allowed to leave the premises unaccompanied.", listOf("are visitors", "visitors are", "were visitors", "visitors were"), "are visitors", "C1", "grammar"),
        RawQuestion("pq_c1_03", "Were it not for your timely intervention, the project _____ collapsed entirely.", listOf("would have", "will have", "had", "should have"), "would have", "C1", "grammar"),
        RawQuestion("pq_c1_04", "The negotiations reached a stalemate, with neither side willing to _____ ground.", listOf("concede", "surrender", "withdraw", "relinquish"), "concede", "C1", "vocabulary"),
        RawQuestion("pq_c1_05", "His subtle sarcastic remarks did not go _____ by the observant committee.", listOf("unnoticed", "unnoticedly", "unnoticing", "without notice"), "unnoticed", "C1", "vocabulary"),
        RawQuestion("pq_c1_06", "No sooner had the keynote speaker begun _____ the microphone failed.", listOf("than", "when", "that", "then"), "than", "C1", "grammar"),
        RawQuestion("pq_c1_07", "It is imperative that every team member _____ compliant with safety protocols.", listOf("remain", "remains", "remained", "shall remain"), "remain", "C1", "grammar"),
        RawQuestion("pq_c1_08", "The sudden economic downturn cast a dark shadow _____ the company's prospects.", listOf("over", "on", "across", "upon"), "over", "C1", "vocabulary"),
        RawQuestion("pq_c1_09", "Much _____ I appreciate your assistance, I must solve this dilemma independently.", listOf("as", "though", "like", "so"), "as", "C1", "grammar"),
        RawQuestion("pq_c1_10", "She has an uncanny knack _____ detecting discrepancies in financial audits.", listOf("for", "of", "to", "in"), "for", "C1", "vocabulary"),
        RawQuestion("pq_c1_11", "The government's new fiscal austerity policy sparked widespread civil _____.", listOf("unrest", "disorderly", "disturb", "commotion"), "unrest", "C1", "vocabulary"),
        RawQuestion("pq_c1_12", "Little _____ realize that their confidential strategy had already been leaked.", listOf("did they", "they did", "had they", "they had"), "did they", "C1", "grammar"),
        RawQuestion("pq_c1_13", "Her argument, while persuasive on the surface, is fundamentally _____.", listOf("flawed", "demolished", "collapsed", "ruptured"), "flawed", "C1", "vocabulary"),
        RawQuestion("pq_c1_14", "Only by scrutinizing every transaction _____ uncover the fraudulent activity.", listOf("could they", "they could", "can they", "they can"), "could they", "C1", "grammar"),
        RawQuestion("pq_c1_15", "The CEO's resignation came out of the blue, leaving staff completely _____.", listOf("flabbergasted", "stun", "flabbergasting", "bewilder"), "flabbergasted", "C1", "vocabulary"),
        RawQuestion("pq_c1_16", "He is prone to making rash decisions, which frequently lands him in hot _____.", listOf("water", "soup", "fire", "steam"), "water", "C1", "vocabulary"),
        RawQuestion("pq_c1_17", "Should you _____ any complications, please do not hesitate to contact support.", listOf("encounter", "encountered", "encounters", "will encounter"), "encounter", "C1", "grammar"),
        RawQuestion("pq_c1_18", "The author's latest essay sheds unprecedented light _____ centuries-old disputes.", listOf("on", "into", "towards", "at"), "on", "C1", "vocabulary"),
        RawQuestion("pq_c1_19", "So intense _____ the heat that the tarmac on the runways began to melt.", listOf("was", "is", "had been", "were"), "was", "C1", "grammar"),
        RawQuestion("pq_c1_20", "He acquitted himself with flying _____ during the rigorous interrogation.", listOf("colors", "banners", "ribbons", "flags"), "colors", "C1", "vocabulary")
    )

    fun seedIfEmpty() {
        if (PlacementQuestionsTable.selectAll().empty()) {
            for (q in questions) {
                val jsonOptions = Json.encodeToString(q.options)
                PlacementQuestionsTable.insertIgnore {
                    it[id] = q.id
                    it[questionText] = q.questionText
                    it[optionsJson] = jsonOptions
                    it[correctOption] = q.correctOption
                    it[level] = q.level
                    it[category] = q.category
                }
            }
        }
    }
}
