package com.eleonorez.cunny.ml

import android.content.Context
import com.eleonorez.cunny.R
import java.util.Locale

class ExplainabilityEngine(context: Context) {
    private val context = context.applicationContext

    /**
     * Compiles raw predictions into a [PredictionResult].
     *
     * @param rawPredictions List of predictions, usually sorted by confidence descending.
     * @param languageCode The ISO language code (e.g., "id" or "en"). Defaults to system locale.
     */
    fun compile(
        rawPredictions: List<LabelConfidence>,
        languageCode: String = getSystemLanguage()
    ): PredictionResult {
        val labels = FruitLabels.load(context)
        
        if (rawPredictions.isEmpty()) {
            val labelRes = if (languageCode == "id") R.string.unknown_label_id else R.string.unknown_label_en
            val rationaleRes = if (languageCode == "id") R.string.unknown_rationale_id else R.string.unknown_rationale_en
            return PredictionResult(
                predictedLabel = context.getString(labelRes),
                confidence = 0f,
                topK = emptyList(),
                rationale = context.getString(rationaleRes)
            )
        }

        // Get top prediction
        val topRawPrediction = rawPredictions[0]
        val topSlug = topRawPrediction.slug
        val topConfidence = topRawPrediction.confidence

        // Find the label entry in our JSON
        val topLabel = labels.find { it.slug.equals(topSlug, ignoreCase = true) }

        // Determine predictedLabel (translated display name)
        val predictedLabel = if (topLabel != null) {
            if (languageCode == "id") topLabel.displayId else topLabel.displayEn
        } else {
            topRawPrediction.label
        }

        // Compile top-3 classes (translating each of them as well)
        val top3Raw = rawPredictions.take(3)
        val topK = top3Raw.map { rawPred ->
            val labelEntry = labels.find { it.slug.equals(rawPred.slug, ignoreCase = true) }
            val translatedLabel = if (labelEntry != null) {
                if (languageCode == "id") labelEntry.displayId else labelEntry.displayEn
            } else {
                rawPred.label
            }
            LabelConfidence(
                label = translatedLabel,
                confidence = rawPred.confidence,
                slug = rawPred.slug
            )
        }

        // Determine rationale (child-friendly reason) with low-confidence handling
        val isLowConf = topConfidence < 0.40f
        val resId = if (languageCode == "id") R.string.low_confidence_rationale_id else R.string.low_confidence_rationale_en
        val rationale = if (isLowConf) {
            context.getString(resId)
        } else {
            if (topLabel != null) {
                if (languageCode == "id") topLabel.rationaleId else topLabel.rationaleEn
            } else {
                val noRationaleRes = if (languageCode == "id") R.string.no_rationale_id else R.string.no_rationale_en
                context.getString(noRationaleRes)
            }
        }

        return PredictionResult(
            predictedLabel = predictedLabel,
            confidence = topConfidence,
            topK = topK,
            rationale = rationale,
            imageSlug = topSlug
        )
    }

    private fun getSystemLanguage(): String {
        val lang = Locale.getDefault().language
        return if (lang == "id" || lang == "in") "id" else "en"
    }
}


