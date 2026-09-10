package br.com.jucelio.sentinelfraud.rules;

public record RuleResult(boolean matched, int score, String reason) {
    public static RuleResult pass() { return new RuleResult(false, 0, ""); }
    public static RuleResult hit(int score, String reason) { return new RuleResult(true, score, reason); }
}
