package task3;

interface DispatchStrategy { boolean shouldDispatch(int severity, int hour); String getStrategyName(); }
class ImmediateStrategy implements DispatchStrategy {
    public boolean shouldDispatch(int severity, int hour) { return true; }
    public String getStrategyName() { return "IMMEDIATE_DISPATCH"; }
}
class BusinessHoursStrategy implements DispatchStrategy {
    public boolean shouldDispatch(int severity, int hour) { return severity >= 5 || (hour >= 9 && hour <= 17); }
    public String getStrategyName() { return "BUSINESS_HOURS_ONLY (09:00 - 17:00)"; }
}
interface MessageChannel { void transmit(String recipient, String message); }
class EmailChannel implements MessageChannel {
    public void transmit(String recipient, String message) { System.out.printf("[EMAIL PROTOCOL -> %s]%n%s%n", recipient, message); }
}
class SmsChannel implements MessageChannel {
    public void transmit(String recipient, String message) { System.out.printf("[SMS PROTOCOL -> %s] %s%n", recipient, message.replace("\n", " -- ")); }
}
abstract class Alert {
    protected final MessageChannel channel;
    protected final DispatchStrategy strategy;
    Alert(MessageChannel channel, DispatchStrategy strategy) {
        if (channel == null || strategy == null) throw new IllegalArgumentException("Dependencies cannot be null");
        this.channel = channel; this.strategy = strategy;
    }
    public void processAlert(String recipient, String rawContent, int currentHour) {
        int severity = getSeverityLevel();
        if (strategy.shouldDispatch(severity, currentHour)) channel.transmit(recipient, formatAlertBody(rawContent));
        else System.out.printf("[DISPATCH SUPPRESSED] Alert blocked by policy '%s' at hour %02d:00 (Severity: %d)%n", strategy.getStrategyName(), currentHour, severity);
    }
    public abstract int getSeverityLevel();
    public abstract String formatAlertBody(String raw);
}
class CriticalSystemAlert extends Alert {
    CriticalSystemAlert(MessageChannel channel, DispatchStrategy strategy) { super(channel, strategy); }
    public int getSeverityLevel() { return 5; }
    public String formatAlertBody(String raw) { return "!!! CRITICAL PRODUCTION ALERT !!!\nIncident Report: " + raw + "\nACTION REQUIRED: SRE on-call immediate intervention."; }
}
class DailySummaryAlert extends Alert {
    DailySummaryAlert(MessageChannel channel, DispatchStrategy strategy) { super(channel, strategy); }
    public int getSeverityLevel() { return 1; }
    public String formatAlertBody(String raw) { return "Daily Digest:\n" + raw; }
}
public class Task3Runner {
    public static void main(String[] args) {
        MessageChannel email = new EmailChannel();
        MessageChannel sms = new SmsChannel();
        DispatchStrategy businessHours = new BusinessHoursStrategy();
        System.out.println(">>> 1. Critical Alert via SMS (Night time: 02:00, Emergency Bypass) <<<");
        new CriticalSystemAlert(sms, businessHours).processAlert("+1555019283", "Database primary node kernel panic", 2);
        System.out.println("\n>>> 2. Daily Summary via Email at Night (Hour: 23:00 - Should Suppress) <<<");
        new DailySummaryAlert(email, businessHours).processAlert("dev-team@company.internal", "99.98% uptime achieved today.", 23);
        System.out.println("\n>>> 3. Daily Summary via Email during Workday (Hour: 11:00 - Should Deliver) <<<");
        new DailySummaryAlert(email, businessHours).processAlert("dev-team@company.internal", "99.98% uptime achieved today.", 11);
    }
}
