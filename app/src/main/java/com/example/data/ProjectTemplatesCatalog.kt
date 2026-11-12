package com.example.data

import org.json.JSONArray
import org.json.JSONObject

object ProjectTemplatesCatalog {

    val builtInTemplates: List<ProjectTemplateModel> = listOf(
        ProjectTemplateModel(
            id = "tpl_blank",
            name = "Blank Workspace",
            category = "Flexible",
            tagline = "Clean slate for any custom workflow",
            description = "Start with an open project canvas and configure your own milestones, missions, and save-point rhythm.",
            colorHex = "#CCFF00",
            iconName = "bookmark",
            defaultMilestones = listOf("Initial Setup", "First Save Point", "Completion Review"),
            defaultMissions = listOf("Define primary outcome", "Capture first working checkpoint"),
            suggestedManualSections = listOf("Current Context", "Next Manual Step", "Open Blockers"),
            savePointPrompts = listOf("Where did I stop?", "What is the immediate next action?"),
            referenceCategories = listOf("General", "Links", "Notes"),
            defaultFocusMode = FocusModeType.POMODORO,
            defaultFocusMinutes = 25
        ),
        ProjectTemplateModel(
            id = "tpl_software",
            name = "Software Development",
            category = "Engineering",
            tagline = "Architecture, implementation, debugging & release checkpoints",
            description = "Tailored for preserving exact file/line context, failing test notes, and branch states across coding sessions.",
            colorHex = "#CCFF00",
            iconName = "terminal",
            defaultMilestones = listOf("Architecture & Schema", "Core Engine Implementation", "Edge-Case & Regression Tests", "Release Readiness"),
            defaultMissions = listOf(
                "Lock data model and invariants",
                "Implement primary happy-path flow",
                "Write deterministic unit tests for edge cases",
                "Audit offline persistence & lifecycle behavior"
            ),
            suggestedManualSections = listOf("Active File & Line Number", "Failing Test / Stack Context", "Next Smallest Code Change"),
            savePointPrompts = listOf("Which file/function was open?", "What test or build check runs next?"),
            referenceCategories = listOf("Specifications", "API Docs", "Code Snippets", "Commands"),
            defaultFocusMode = FocusModeType.DEEP_WORK,
            defaultFocusMinutes = 50
        ),
        ProjectTemplateModel(
            id = "tpl_school",
            name = "School Project",
            category = "Academic",
            tagline = "Coursework, lab reports, readings & submission milestones",
            description = "Keep syllabus requirements, reading citations, draft sections, and exam prep organized without losing your place.",
            colorHex = "#60A5FA",
            iconName = "school",
            defaultMilestones = listOf("Topic & Rubric Breakdown", "Primary Source Reading", "First Complete Draft", "Final Proof & Submission"),
            defaultMissions = listOf(
                "Extract grading rubric requirements into checklist",
                "Summarize 3 core readings with page numbers",
                "Draft body section 1",
                "Verify formatting and bibliography"
            ),
            suggestedManualSections = listOf("Current Page / Lecture Timestamp", "Next Paragraph or Problem Set"),
            savePointPrompts = listOf("Which page or problem number did I finish?", "What is needed before the next study block?"),
            referenceCategories = listOf("Syllabus & Rubric", "Lecture Notes", "Citations", "Handouts"),
            defaultFocusMode = FocusModeType.POMODORO,
            defaultFocusMinutes = 25
        ),
        ProjectTemplateModel(
            id = "tpl_personal_goal",
            name = "Personal Goal",
            category = "Life",
            tagline = "Intentional milestones and low-friction momentum",
            description = "Break a meaningful personal ambition into clear checkpoints that are easy to resume after busy weeks.",
            colorHex = "#34D399",
            iconName = "flag",
            defaultMilestones = listOf("Baseline & Preparation", "First 30% Momentum", "Midpoint Checkpoint", "Goal Achieved"),
            defaultMissions = listOf(
                "Clarify concrete definition of done",
                "Prepare environment and tools",
                "Complete first 15-minute starter session"
            ),
            suggestedManualSections = listOf("Current Momentum State", "Next 10-Minute Action"),
            savePointPrompts = listOf("What progress did I make today?", "How can I make restarting tomorrow effortless?"),
            referenceCategories = listOf("Inspiration", "Checklists", "Resources"),
            defaultFocusMode = FocusModeType.POMODORO,
            defaultFocusMinutes = 25
        ),
        ProjectTemplateModel(
            id = "tpl_creative",
            name = "Creative Project",
            category = "Design & Art",
            tagline = "Concept exploration, iteration, composition & polish",
            description = "Preserve creative direction, palette/mood notes, and exact layer or scene state between studio sessions.",
            colorHex = "#F472B6",
            iconName = "palette",
            defaultMilestones = listOf("Moodboard & Direction", "Rough Composition / Prototype", "Refinement Pass", "Final Export & Archive"),
            defaultMissions = listOf(
                "Gather 5 visual/audio reference anchors",
                "Block out primary silhouette or structure",
                "Refine contrast, pacing, and details"
            ),
            suggestedManualSections = listOf("Active Canvas / Timeline Timestamp", "Next Creative Pass"),
            savePointPrompts = listOf("What aesthetic decision was just made?", "Which section needs attention next?"),
            referenceCategories = listOf("Moodboard", "Color & Type", "Asset Links", "Technical Notes"),
            defaultFocusMode = FocusModeType.DEEP_WORK,
            defaultFocusMinutes = 45
        ),
        ProjectTemplateModel(
            id = "tpl_research",
            name = "Research",
            category = "Knowledge",
            tagline = "Literature synthesis, hypotheses, evidence & findings",
            description = "Track open questions, primary documents, methodology notes, and exact reading positions.",
            colorHex = "#A78BFA",
            iconName = "science",
            defaultMilestones = listOf("Research Question & Scope", "Source Collection", "Evidence Synthesis", "Findings Summary"),
            defaultMissions = listOf(
                "Formulate primary research question",
                "Catalog 5 primary sources in Library",
                "Synthesize conflicting evidence",
                "Write executive findings note"
            ),
            suggestedManualSections = listOf("Current Paper / Archive Box", "Open Hypothesis or Question"),
            savePointPrompts = listOf("Which source was I analyzing?", "What question should I answer next session?"),
            referenceCategories = listOf("Primary Sources", "Datasets", "Methodology", "Quotes & Snippets"),
            defaultFocusMode = FocusModeType.DEEP_WORK,
            defaultFocusMinutes = 50
        ),
        ProjectTemplateModel(
            id = "tpl_writing",
            name = "Writing Project",
            category = "Editorial",
            tagline = "Outlines, chapter drafts, structural edits & line polish",
            description = "Never stare at a blank cursor wondering where your train of thought was heading—save the next sentence before closing.",
            colorHex = "#FBBF24",
            iconName = "menu_book",
            defaultMilestones = listOf("Outline & Thesis", "Zero Draft Complete", "Structural Revision", "Copyedit & Final Manuscript"),
            defaultMissions = listOf(
                "Outline section beats and key arguments",
                "Draft opening section without self-editing",
                "Complete structural transitions",
                "Read aloud for rhythm and clarity"
            ),
            suggestedManualSections = listOf("Current Section & Word Count", "Unwritten Next Sentence"),
            savePointPrompts = listOf("Where in the draft did I stop?", "What is the very next idea to write?"),
            referenceCategories = listOf("Outline", "Research Clips", "Style Guide", "Cut Scenes / Scraps"),
            defaultFocusMode = FocusModeType.DEEP_WORK,
            defaultFocusMinutes = 45
        ),
        ProjectTemplateModel(
            id = "tpl_business",
            name = "Business Project",
            category = "Operations",
            tagline = "Strategy, deliverables, stakeholder blockers & launch execution",
            description = "Keep deliverables, dependencies, decision logs, and external blockers crystal clear.",
            colorHex = "#38BDF8",
            iconName = "business_center",
            defaultMilestones = listOf("Scope & Brief", "Core Deliverables", "Operational Review", "Launch & Handover"),
            defaultMissions = listOf(
                "Define scope, constraints, and success criteria",
                "Draft core operational checklist",
                "Resolve top external blocker",
                "Complete pre-launch verification"
            ),
            suggestedManualSections = listOf("Decision Context", "External Blockers", "Next Concrete Deliverable"),
            savePointPrompts = listOf("What decision or deliverable was completed?", "What is blocking the critical path?"),
            referenceCategories = listOf("Briefs", "Financials / Specs", "Checklists", "Key Links"),
            defaultFocusMode = FocusModeType.POMODORO,
            defaultFocusMinutes = 30
        ),
        ProjectTemplateModel(
            id = "tpl_home",
            name = "Home Improvement",
            category = "Hands-On",
            tagline = "Measurements, materials list, tool prep & build phases",
            description = "Ideal for weekend workshop or renovation projects that pause for days between work sessions.",
            colorHex = "#FB923C",
            iconName = "architecture",
            defaultMilestones = listOf("Measurements & Cut List", "Materials & Tool Prep", "Primary Assembly", "Finish & Cleanup"),
            defaultMissions = listOf(
                "Measure space and verify dimensions twice",
                "Create materials and hardware checklist",
                "Complete dry-fit assembly",
                "Apply finish and inspect"
            ),
            suggestedManualSections = listOf("Exact Measurement / Step Reached", "Tools & Materials Needed Next"),
            savePointPrompts = listOf("Which physical step was just finished?", "What tool or part do I pick up first next time?"),
            referenceCategories = listOf("Dimensions & CAD", "Materials List", "Safety Checklists", "Manuals"),
            defaultFocusMode = FocusModeType.STOPWATCH,
            defaultFocusMinutes = 45
        ),
        ProjectTemplateModel(
            id = "tpl_learning",
            name = "Learning Roadmap",
            category = "Mastery",
            tagline = "Modules, practice exercises, notes & synthesis checkpoints",
            description = "Remember the exact module, timestamp, and practice exercise when mastering a new technical or creative skill.",
            colorHex = "#2DD4BF",
            iconName = "lightbulb",
            defaultMilestones = listOf("Foundations & Environment", "Core Concepts Practice", "Guided Capstone", "Independent Synthesis"),
            defaultMissions = listOf(
                "Set up practice sandbox",
                "Complete Module 1 active recall notes",
                "Build small hands-on exercise without looking at guide",
                "Document key mental models in Library"
            ),
            suggestedManualSections = listOf("Chapter / Video Timestamp", "Concept Currently Practicing"),
            savePointPrompts = listOf("What concept just clicked?", "Which exercise do I tackle next?"),
            referenceCategories = listOf("Curriculum", "Cheat Sheets", "Practice Notes", "Exercises"),
            defaultFocusMode = FocusModeType.POMODORO,
            defaultFocusMinutes = 25
        ),
        ProjectTemplateModel(
            id = "tpl_challenge",
            name = "Personal Challenge",
            category = "Consistency",
            tagline = "Structured multi-stage challenge with forgiving recovery",
            description = "Track a multi-session personal challenge where pausing is normal and resuming via Rescue Mode is effortless.",
            colorHex = "#E879F9",
            iconName = "bolt",
            defaultMilestones = listOf("Phase 1: Kickoff", "Phase 2: Rhythm", "Phase 3: Deepening", "Challenge Complete"),
            defaultMissions = listOf(
                "Complete Session 1 and log initial checkpoint",
                "Prepare low-friction 10-minute fallback action",
                "Complete midpoint review checkpoint"
            ),
            suggestedManualSections = listOf("Current Session Number", "Smallest Possible Next Step"),
            savePointPrompts = listOf("What worked well in this session?", "What is the easiest entry point for next time?"),
            referenceCategories = listOf("Rules & Scope", "Session Logs", "Resources"),
            defaultFocusMode = FocusModeType.POMODORO,
            defaultFocusMinutes = 20
        )
    )

    fun exportTemplateToJson(template: ProjectTemplateModel): String {
        return JSONObject().apply {
            put("app", "ReturnmarkTemplate")
            put("id", template.id)
            put("name", template.name)
            put("category", template.category)
            put("tagline", template.tagline)
            put("description", template.description)
            put("colorHex", template.colorHex)
            put("iconName", template.iconName)
            put("defaultMilestones", JSONArray(template.defaultMilestones))
            put("defaultMissions", JSONArray(template.defaultMissions))
            put("defaultFocusMode", template.defaultFocusMode.name)
            put("defaultFocusMinutes", template.defaultFocusMinutes)
        }.toString(2)
    }

    fun parseImportedTemplateJson(rawJson: String): ProjectTemplateModel? {
        return try {
            val obj = JSONObject(rawJson)
            val name = obj.optString("name", "").trim()
            if (name.isEmpty()) return null
            val milestonesArr = obj.optJSONArray("defaultMilestones") ?: JSONArray()
            val milestones = (0 until milestonesArr.length()).map { milestonesArr.getString(it) }
            val missionsArr = obj.optJSONArray("defaultMissions") ?: JSONArray()
            val missions = (0 until missionsArr.length()).map { missionsArr.getString(it) }
            ProjectTemplateModel(
                id = "custom_${System.currentTimeMillis()}",
                name = name,
                category = obj.optString("category", "Custom"),
                tagline = obj.optString("tagline", "Imported Returnmark Template"),
                description = obj.optString("description", ""),
                colorHex = obj.optString("colorHex", "#CCFF00"),
                iconName = obj.optString("iconName", "bookmark"),
                defaultMilestones = milestones.ifEmpty { listOf("Milestone 1", "Milestone 2") },
                defaultMissions = missions.ifEmpty { listOf("Initial Mission") },
                suggestedManualSections = listOf("Current Context", "Next Step"),
                savePointPrompts = listOf("Where did I stop?"),
                referenceCategories = listOf("General"),
                defaultFocusMode = FocusModeType.entries.find { it.name == obj.optString("defaultFocusMode") }
                    ?: FocusModeType.POMODORO,
                defaultFocusMinutes = obj.optInt("defaultFocusMinutes", 25),
                isBuiltIn = false
            )
        } catch (_: Exception) {
            null
        }
    }
}
