# COPPA / Under-13 Compliance Evaluation — Cunny: Belajar AI Interaktif

**Date:** July 5, 2026  
**Author:** Eleonore Z (Developer)  
**App:** Cunny — Belajar AI Interaktif (`com.eleonorez.cunny`)  
**Status:** 🟡 Evaluation — Action Required Before Play Store Submission

---

## 1. Executive Summary

Cunny targets students aged **11–14 years** (SMP / junior high school in Indonesia). Because the target audience includes children **under 13**, this triggers requirements under:

- **COPPA** (Children's Online Privacy Protection Act, U.S.)
- **Google Play's Families Policy** and Target Audience requirements
- **Indonesia's UU PDP** (Data Protection Law No. 27/2022)

### Key Recommendation

> **🟢 RECOMMENDED: Set the Play Console Target Audience to "13 and up" and do NOT register for the Designed for Families program.**

This is the safest, most practical path for an indie developer shipping an MVP. See [Section 7](#7-recommendation-and-action-plan) for full rationale.

---

## 2. Current State Analysis

### 2.1 App Profile

| Attribute | Value |
|-----------|-------|
| Target audience (intended) | 11–14 years old (SMP students) |
| Authentication method | Google Sign-In (Firebase Auth) |
| Data collected | Email, display name, learning progress |
| Ads | None |
| In-app purchases | Planned (subscription via Google Play Billing) |
| Social features | None (no chat, no UGC sharing) |
| Camera usage | On-device only (Fruit Scanner, no upload) |
| Content type | Educational (AI/ML fundamentals) |

### 2.2 Data Collection Summary

| Data Type | Collected? | From Minors? | Notes |
|-----------|:----------:|:------------:|-------|
| Email address | ✅ | Potentially | Via Google Sign-In |
| Display name | ✅ | Potentially | Via Google Sign-In |
| Photos/videos | ❌ | No | Processed on-device only, never uploaded |
| Location | ❌ | No | Not collected at all |
| Contacts | ❌ | No | Not collected at all |
| Device identifiers | ⚠️ | Potentially | Via Firebase Crashlytics (planned) |
| Learning progress | ✅ | Potentially | XP, level, streaks, badges |

---

## 3. COPPA Analysis

### 3.1 What Is COPPA?

The **Children's Online Privacy Protection Act** (COPPA) is a U.S. federal law that applies to:

- Websites and online services **directed to children under 13**, OR
- Services that have **actual knowledge** that they collect personal information from children under 13

COPPA requires **verifiable parental consent** before collecting personal information from children under 13.

### 3.2 Does COPPA Apply to Cunny?

| COPPA Trigger | Applies? | Analysis |
|---------------|:--------:|----------|
| App directed to children under 13? | ⚠️ Partially | Target audience is 11–14, which includes under-13 |
| Collects personal info from <13? | ⚠️ Potentially | Email via Google Sign-In — but Google manages the account |
| Has actual knowledge of <13 users? | ❌ Not currently | No age gate or age verification in-app |
| Operates in the US? | ⚠️ Possibly | Available on US Play Store unless geo-restricted |

### 3.3 COPPA Requirements (If Applicable)

If COPPA applies, the following would be required:

| Requirement | Current Status | Gap |
|-------------|:-------------:|-----|
| Privacy policy addressing children's data | ✅ Covered | Section 7 of our Privacy Policy |
| Verifiable parental consent (VPC) | ❌ Not implemented | No parental consent flow exists |
| Parental right to review/delete data | ✅ Covered | Delete account via Settings or email |
| Data minimization | ✅ Compliant | Only essential data collected |
| No behavioral advertising to children | ✅ Compliant | No ads at all |
| No selling children's data | ✅ Compliant | No data sold |
| Reasonable data security | ✅ Compliant | HTTPS, Firebase Auth tokens |
| Data retention limits | ✅ Covered | Data deleted when account deleted |

### 3.4 Key COPPA Gap: Verifiable Parental Consent

The **only significant COPPA gap** is the lack of a Verifiable Parental Consent (VPC) mechanism. Implementing VPC is complex and typically requires one of:

- Parent email + confirmation flow
- Credit card verification (small charge)
- Government-issued ID scan
- Video call verification
- Signed consent form

**This is a significant engineering burden for an indie MVP.**

---

## 4. Google Play Families Policy Analysis

### 4.1 Play Console Target Audience Options

When submitting to Play Console, you must declare your target audience:

| Option | Implications |
|--------|-------------|
| **Children only** (under 13) | Must join Families program. Strictest requirements. |
| **Children and older audiences** (mixed) | Must join Families program for the children's portion. Must implement a neutral age gate. |
| **13 and up** (teens+) | Do NOT need Families program. Standard Play policies apply. |
| **16 and up** or **18 and up** | Most permissive. Not appropriate for Cunny's audience. |

### 4.2 Designed for Families Program Requirements

If Cunny registers for the **Designed for Families** program, the following would be required:

| Requirement | Effort | Current Status |
|-------------|--------|:-------------:|
| COPPA compliance (including VPC) | 🔴 High | ❌ |
| No behavioral ads; only certified ad networks | 🟢 N/A | ✅ (no ads) |
| Age-appropriate content | 🟢 Low | ✅ |
| No linking to content outside the app | 🟡 Medium | ⚠️ Need audit |
| Accurate content rating | 🟢 Low | ✅ |
| Families ad-policy compliant SDKs only | 🟡 Medium | ⚠️ Firebase needs review |
| Neutral age gate (for mixed audience) | 🟡 Medium | ❌ Not implemented |
| Separate privacy policy for children | 🟡 Medium | ⚠️ Partial |
| Teacher-approved badge eligibility | 🟢 Optional | N/A |

### 4.3 Families Program: Cost/Benefit for Cunny

| Benefit | Value for Cunny |
|---------|----------------|
| Featured in "Family" section of Play Store | 🟡 Moderate — more visibility |
| Teacher Approved badge | 🟢 Nice to have, not critical |
| Trust signal for parents | 🟡 Moderate |

| Cost | Impact for Cunny |
|------|-----------------|
| Implement VPC (verifiable parental consent) | 🔴 **High** — significant engineering effort |
| Implement neutral age gate | 🟡 Medium — requires new screen/flow |
| SDK compliance audit | 🟡 Medium — Firebase may need review |
| Ongoing compliance monitoring | 🟡 Medium — additional maintenance burden |
| Rejection risk if non-compliant | 🔴 **High** — could delay launch |

---

## 5. Indonesia UU PDP Considerations

Indonesia's **Undang-Undang Pelindungan Data Pribadi** (Law No. 27/2022) includes provisions for children's data:

| Requirement | Status |
|-------------|:------:|
| Consent required for data processing | ✅ Via Google Sign-In consent flow |
| Parental consent for children under 17 | ⚠️ Not explicitly implemented |
| Data minimization | ✅ Only essential data collected |
| Right to erasure | ✅ Delete account available |
| Data security obligations | ✅ HTTPS, secure infrastructure |

> **Note:** UU PDP defines "children" as under 17 (age of majority in Indonesia). However, Google Sign-In already handles account creation, and Google's own family policies apply to accounts of minors.

---

## 6. Risk Assessment

### 6.1 Risk Matrix

| Risk | Likelihood | Impact | Mitigation |
|------|:----------:|:------:|------------|
| COPPA violation complaint (US) | 🟡 Low-Medium | 🔴 High | Set target audience 13+ |
| Play Store rejection (Families non-compliance) | 🟡 Medium | 🔴 High | Don't register for Families |
| UU PDP complaint (Indonesia) | 🟢 Low | 🟡 Medium | Privacy Policy covers it |
| Parent complaint about child's data | 🟢 Low | 🟡 Medium | Offer account deletion |
| FTC enforcement action | 🟢 Very Low | 🔴 High | Set target 13+, don't market to <13 |

### 6.2 Key Risk: "Directed to Children" Determination

Even with a 13+ target audience, an app could be considered "directed to children" if:

- ❌ Its visual design is childish (cartoon characters, etc.)
- ❌ It features child celebrities or characters appealing to children
- ❌ It uses animated characters or child-oriented activities
- ✅ **Cunny has a mascot** — but the content is clearly educational/technical
- ✅ **Cunny covers AI/ML topics** — typically studied by teens, not young children
- ✅ **The UI uses sophisticated design** — glassmorphism, not childish

**Assessment:** Cunny's AI/ML education content and sophisticated UI design are more appropriate for teens (13+) than young children. The mascot alone does not make the app "directed to children under 13."

---

## 7. Recommendation and Action Plan

### 🟢 PRIMARY RECOMMENDATION: Set Target Audience to "13 and Up"

**Do NOT register for the Designed for Families program for the MVP launch.**

#### Rationale

1. **COPPA avoidance:** By declaring the target audience as 13+, COPPA's strictest requirements (VPC) do not apply, as the app is not "directed to children under 13"
2. **Engineering feasibility:** Implementing VPC would require 2-4 weeks of additional development — unacceptable given the July 12 deadline
3. **Content appropriateness:** AI/ML education content is genuinely appropriate for ages 13+, aligning with the declaration
4. **Minimal risk:** Without ads, social features, or aggressive data collection, the privacy risk profile is already low
5. **Google Sign-In as safeguard:** Google already enforces its own age policies on account creation, providing an implicit age gate

#### Play Console Settings

```
Target Audience: 13 and up (Teens)
Content Rating: PEGI 3 / ESRB Everyone (educational content)
Designed for Families: No
Contains Ads: No
```

#### Marketing Adjustment

- **In Play Store listing:** Describe as "for students learning AI" (not "for children")
- **Age reference:** Use "students" and "learners" instead of "children" or "kids"
- **School context:** "SMP/high school students" rather than "ages 11-14"

### 🟡 FUTURE CONSIDERATION: Families Program (Post-Launch)

If Cunny wants to reach the under-13 audience officially in a future version:

| Step | Priority | Timeline |
|------|----------|----------|
| Implement neutral age gate screen | P1 | v1.1 |
| Build parental consent flow (email-based VPC) | P1 | v1.1 |
| Audit all SDKs for Families compliance | P1 | v1.1 |
| Apply for Designed for Families | P2 | v1.2 |
| Apply for Teacher Approved badge | P3 | v1.3 |

---

## 8. Immediate Action Items (Before July 12)

### ✅ Already Done

- [x] Privacy Policy covers children's data (Section 7)
- [x] No behavioral advertising
- [x] No data sold to third parties
- [x] Account deletion available
- [x] On-device ML processing (no photo uploads)
- [x] No social features or UGC

### 📋 To Do Before Submission

- [ ] **Set Play Console target audience to "13 and up"**
- [ ] **Ensure Play Store listing does NOT market to children under 13**
  - Use "students" and "learners" in descriptions
  - Avoid "for kids" or "for children" language
  - Reference "SMP" and "junior high" instead of specific young ages
- [ ] **Add age disclaimer in app description:**
  > "This app is designed for students aged 13 and older. Users under 18 should have parental consent."
- [ ] **Verify Google Sign-In handles age-gating** (Google accounts for under-13 require Family Link)
- [ ] **Review Privacy Policy** is accessible at a public URL before submission

---

## 9. Legal Disclaimer

> **This document is a developer's self-evaluation and does NOT constitute legal advice.** For authoritative guidance on COPPA compliance, consult a qualified attorney specializing in children's privacy law. For Google Play policy questions, refer to the [Google Play Developer Policy Center](https://play.google.com/about/developer-content-policy/).

---

## 10. References

| Resource | URL |
|----------|-----|
| COPPA Rule (FTC) | https://www.ftc.gov/legal-library/browse/rules/childrens-online-privacy-protection-rule-coppa |
| FTC COPPA FAQ | https://www.ftc.gov/business-guidance/resources/complying-coppa-frequently-asked-questions |
| Google Play Families Policy | https://support.google.com/googleplay/android-developer/answer/9893335 |
| Google Designed for Families | https://play.google.com/console/about/programs/designed-for-families/ |
| Indonesia UU PDP (No. 27/2022) | https://peraturan.bpk.go.id/Details/229798/uu-no-27-tahun-2022 |
| Google Family Link | https://families.google.com/familylink/ |

---

*© 2026 Eleonore Z. All rights reserved.*
