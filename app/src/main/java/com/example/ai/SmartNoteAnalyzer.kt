package com.example.ai

import java.util.Locale

object SmartNoteAnalyzer {

    data class AiAnalysisResult(
        val suggestedTags: List<String>,
        val summary: String,
        val actionItems: List<String>,
        val keyConcepts: List<String>
    )

    fun analyzeNote(title: String, content: String): AiAnalysisResult {
        val fullText = "$title\n$content".lowercase(Locale.ROOT)
        val suggestedTags = mutableSetOf<String>()

        // Subject recognition
        if (fullText.contains("algorithm") || fullText.contains("complexity") || fullText.contains("dp") || fullText.contains("code") || fullText.contains("function")) {
            suggestedTags.add("cs101")
            suggestedTags.add("algorithms")
        }
        if (fullText.contains("dna") || fullText.contains("rna") || fullText.contains("crispr") || fullText.contains("gene") || fullText.contains("cell") || fullText.contains("biology")) {
            suggestedTags.add("biology")
            suggestedTags.add("genetics")
        }
        if (fullText.contains("calculus") || fullText.contains("matrix") || fullText.contains("vector") || fullText.contains("eigen") || fullText.contains("integral") || fullText.contains("formula")) {
            suggestedTags.add("calculus")
            suggestedTags.add("math")
        }

        // Academic context recognition
        if (fullText.contains("exam") || fullText.contains("midterm") || fullText.contains("final") || fullText.contains("quiz") || fullText.contains("test")) {
            suggestedTags.add("exam_prep")
        }
        if (fullText.contains("lecture") || fullText.contains("professor") || fullText.contains("class") || fullText.contains("chapter")) {
            suggestedTags.add("lecture")
        }
        if (fullText.contains("lab") || fullText.contains("protocol") || fullText.contains("experiment")) {
            suggestedTags.add("lab_work")
        }
        if (fullText.contains("homework") || fullText.contains("assignment") || fullText.contains("due") || fullText.contains("deadline")) {
            suggestedTags.add("assignments")
        }
        if (fullText.contains("important") || fullText.contains("key") || fullText.contains("remember") || fullText.contains("note")) {
            suggestedTags.add("key_takeaway")
        }

        // Fallbacks if text is short
        if (suggestedTags.isEmpty()) {
            suggestedTags.add("study_notes")
            suggestedTags.add("student")
        }

        // Summary generation from clean sentences
        val sentences = content.split(".").map { it.trim() }.filter { it.length > 15 }
        val summary = if (sentences.isNotEmpty()) {
            val primary = sentences.take(2).joinToString(". ") + "."
            "Summary: $primary"
        } else {
            "Summary: Overview of $title focusing on fundamental student principles and exam preparation."
        }

        // Extract action items
        val actionItems = mutableListOf<String>()
        val lines = content.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("[ ]") || trimmed.startsWith("[x]")) {
                actionItems.add(trimmed.substring(3).trim())
            } else if (trimmed.startsWith("- ") && (trimmed.contains("review", ignoreCase = true) || trimmed.contains("implement", ignoreCase = true) || trimmed.contains("solve", ignoreCase = true))) {
                actionItems.add(trimmed.substring(2).trim())
            }
        }
        if (actionItems.isEmpty()) {
            actionItems.add("Review core formulas and key definitions for $title")
            actionItems.add("Complete practice exercises before next lecture")
        }

        // Key concepts
        val concepts = mutableListOf<String>()
        val headerLines = lines.filter { it.startsWith("#") }.map { it.replace("#", "").trim() }
        if (headerLines.isNotEmpty()) {
            concepts.addAll(headerLines.take(3))
        } else {
            concepts.add("Core Fundamentals")
            concepts.add("Exam Applications")
        }

        return AiAnalysisResult(
            suggestedTags = suggestedTags.toList(),
            summary = summary,
            actionItems = actionItems,
            keyConcepts = concepts
        )
    }
}
