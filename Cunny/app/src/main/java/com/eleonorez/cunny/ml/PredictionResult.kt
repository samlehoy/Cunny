package com.eleonorez.cunny.ml

data class LabelConfidence(
    val label: String, // Display name (e.g., "Apple" or "Apel")
    val confidence: Float,
    val slug: String // Original class slug (e.g., "apple")
)

data class PredictionResult(
    val predictedLabel: String,       // Translated display name
    val confidence: Float,            // Top confidence [0..1]
    val topK: List<LabelConfidence>,  // Top-K predictions (typically top 3)
    val rationale: String,            // Child-friendly reasoning
    val imageSlug: String? = null     // Optional slug for the predicted fruit image reference
)


