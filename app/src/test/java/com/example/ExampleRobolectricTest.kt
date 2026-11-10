package com.example

import com.example.ai.SmartNoteAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `smart note analyzer generates relevant tags for algorithms`() {
        val result = SmartNoteAnalyzer.analyzeNote(
            title = "Dynamic Programming & Complexity",
            content = "In this lecture we analyze algorithmic recurrence and memoization for the midterm exam."
        )
        assertTrue(result.suggestedTags.contains("cs101"))
        assertTrue(result.suggestedTags.contains("algorithms"))
        assertTrue(result.suggestedTags.contains("exam_prep"))
        assertTrue(result.actionItems.isNotEmpty())
    }

    @Test
    fun `smart note analyzer extracts biology genetics tags`() {
        val result = SmartNoteAnalyzer.analyzeNote(
            title = "Molecular Biology & CRISPR",
            content = "CRISPR-Cas9 guide RNA and double strand break mechanisms in gene editing."
        )
        assertTrue(result.suggestedTags.contains("biology"))
        assertTrue(result.suggestedTags.contains("genetics"))
    }
}
