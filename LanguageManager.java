public class LanguageManager {
    public static final String ENGLISH = "en";
    public static final String HINDI = "hi";
    
    private static String currentLanguage = ENGLISH;
    
    public static void setLanguage(String lang) {
        currentLanguage = lang;
    }
    
    public static String getLanguage() {
        return currentLanguage;
    }
    
    public static boolean isHindi() {
        return currentLanguage.equals(HINDI);
    }
    
    public static boolean isEnglish() {
        return currentLanguage.equals(ENGLISH);
    }
    
    // Text translations
    public static String getText(String key) {
        if (isHindi()) {
            return getHindiText(key);
        } else {
            return getEnglishText(key);
        }
    }
    
    private static String getEnglishText(String key) {
        switch(key) {
            case "title": return "ATM SYSTEM";
            case "card_number": return "Card Number:";
            case "pin": return "PIN:";
            case "login": return "Login";
            case "forgot_pin": return "Forgot PIN";
            case "balance": return "Balance";
            case "deposit": return "Deposit";
            case "withdraw": return "Withdraw";
            case "ministatement": return "Mini Statement";
            case "exit": return "Exit";
            case "amount": return "Amount";
            case "enter_amount": return "Enter Amount";
            case "invalid_amount": return "Invalid amount";
            case "insufficient_balance": return "Insufficient balance";
            case "transaction_success": return "Transaction successful";
            case "wrong_otp": return "Wrong OTP";
            case "enter_otp": return "Enter OTP";
            case "select_language": return "Select Language";
            case "english": return "English";
            case "hindi": return "Hindi";
            case "thank_you": return "Thank you for using our ATM";
            case "current_balance": return "Current Balance:";
            case "no_transactions": return "No transactions found";
            case "invalid_card_pin": return "Invalid Card/PIN";
            default: return key;
        }
    }
    
    private static String getHindiText(String key) {
        switch(key) {
            case "title": return "एटीएम सिस्टम";
            case "card_number": return "कार्ड संख्या:";
            case "pin": return "पिन:";
            case "login": return "लॉग इन";
            case "forgot_pin": return "पिन भूल गया";
            case "balance": return "बैलेंस";
            case "deposit": return "जमा करें";
            case "withdraw": return "निकालें";
            case "ministatement": return "मिनी स्टेटमेंट";
            case "exit": return "बाहर निकलें";
            case "amount": return "राशि";
            case "enter_amount": return "राशि दर्ज करें";
            case "invalid_amount": return "अमान्य राशि";
            case "insufficient_balance": return "अपर्याप्त शेष";
            case "transaction_success": return "लेन-देन सफल";
            case "wrong_otp": return "गलत ओटीपी";
            case "enter_otp": return "ओटीपी दर्ज करें";
            case "select_language": return "भाषा चुनें";
            case "english": return "अंग्रेजी";
            case "hindi": return "हिंदी";
            case "thank_you": return "हमारे एटीएम का उपयोग करने के लिए धन्यवाद";
            case "current_balance": return "वर्तमान शेष:";
            case "no_transactions": return "कोई लेन-देन नहीं मिली";
            case "invalid_card_pin": return "अमान्य कार्ड/पिन";
            default: return key;
        }
    }
}
