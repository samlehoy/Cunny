package com.eleonorez.cunny.ml

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.InputStreamReader

data class FruitLabel(
    val slug: String,
    @SerializedName("display_en") val displayEn: String,
    @SerializedName("display_id") val displayId: String,
    @SerializedName("rationale_en") val rationaleEn: String,
    @SerializedName("rationale_id") val rationaleId: String
)

data class FruitLabelsContainer(
    val classes: List<FruitLabel>
)

object FruitLabels {
    private var labelsContainer: FruitLabelsContainer? = null

    fun load(context: Context): List<FruitLabel> {
        return synchronized(this) {
            val container = labelsContainer
            if (container != null) {
                container.classes
            } else {
                var loaded: FruitLabelsContainer? = null
                try {
                    context.applicationContext.assets.open("fruit_labels.json").use { inputStream ->
                        InputStreamReader(inputStream).use { reader ->
                            loaded = Gson().fromJson(reader, FruitLabelsContainer::class.java)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FruitLabels", "Failed to load fruit labels", e)
                }
                labelsContainer = loaded
                loaded?.classes ?: emptyList()
            }
        }
    }

    @VisibleForTesting
    fun clearCache() {
        synchronized(this) {
            labelsContainer = null
        }
    }
}


