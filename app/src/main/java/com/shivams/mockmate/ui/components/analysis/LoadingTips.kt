package com.shivams.mockmate.ui.components.analysis

/**
 * Loading Tips and Quotes for the SmartLoadingScreen.
 * Keeps users engaged during long analysis times.
 */
object LoadingTips {
    
    /**
     * Motivational quotes for UPSC aspirants
     */
    val quotes = listOf(
        "\"Success is not final, failure is not fatal: It is the courage to continue that counts.\" — Winston Churchill",
        "\"The only way to do great work is to love what you do.\" — Steve Jobs",
        "\"Hard work beats talent when talent doesn't work hard.\" — Tim Notke",
        "\"It does not matter how slowly you go as long as you do not stop.\" — Confucius",
        "\"The future belongs to those who believe in the beauty of their dreams.\" — Eleanor Roosevelt",
        "\"Believe you can and you're halfway there.\" — Theodore Roosevelt",
        "\"Every expert was once a beginner.\" — Helen Hayes",
        "\"A river cuts through rock not because of its power, but because of its persistence.\"",
        "\"Dreams are not seen when you sleep. Dreams are those that don't let you sleep.\" — APJ Abdul Kalam",
        "\"The best preparation for tomorrow is doing your best today.\" — H. Jackson Brown Jr."
    )
    
    /**
     * UPSC Prelims traps and facts
     */
    val prelimsFacts = listOf(
        "🔔 Trap Alert: The Constitution does NOT define 'Violation of Constitution' for the President's impeachment.",
        "💡 Fact: Article 1 says India is a 'Union of States', not a 'Federation of States'.",
        "⚠️ Common Mistake: The PM is not constitutionally required to be a member of Lok Sabha.",
        "🔔 Trap: Fundamental Rights are NOT absolute — they can be restricted by law.",
        "💡 Note: Zero Hour is not mentioned in Rules of Procedure — it's a parliamentary convention.",
        "⚠️ Tricky: The Finance Bill is introduced in Lok Sabha only, but Money Bill is certified by the Speaker.",
        "🔔 Trick Question: DPSP are non-justiciable but not optional for the State.",
        "💡 Remember: President's Rule under Article 356 requires Parliamentary approval within 2 months.",
        "⚠️ Often Confused: Emergency provisions were borrowed from Weimar Constitution (Germany).",
        "🔔 Common Error: The Attorney General is not a member of the cabinet."
    )
    
    /**
     * Analysis status messages that cycle during loading
     */
    val statusMessages = listOf(
        "📤 Uploading PDF to secure server...",
        "🧠 Waking up AI Mentor...",
        "🔍 Scanning your handwritten annotations...",
        "🎨 Detecting ink colors and patterns...",
        "📊 Analyzing cognitive patterns...",
        "✍️ Understanding your elimination strategy...",
        "💭 Evaluating thinking process...",
        "📝 Generating personalized feedback...",
        "🎯 Calculating UPSC score...",
        "✨ Preparing your insights dashboard..."
    )
    
    /**
     * Get a random quote
     */
    fun getRandomQuote(): String = quotes.random()
    
    /**
     * Get a random UPSC fact/trap
     */
    fun getRandomFact(): String = prelimsFacts.random()
    
    /**
     * Get a combined tip (alternates between quotes and facts)
     */
    fun getRandomTip(): String = if ((0..1).random() == 0) getRandomQuote() else getRandomFact()
}
