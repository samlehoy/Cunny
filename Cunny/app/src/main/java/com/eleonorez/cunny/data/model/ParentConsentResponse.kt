package com.eleonorez.cunny.data.model

data class ParentConsentRequest(
    val child_name: String,
    val parent_email: String,
    val birth_year: Int
)

data class ParentConsentResponse(
    val error: Boolean?,
    val message: String?
)
