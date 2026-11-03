package task4;

class LegacyForeignOrder {
    private final String orderId; private final double gbpAmount;
    LegacyForeignOrder(String orderId, double gbpAmount) { this.orderId = orderId; this.gbpAmount = gbpAmount; }
    public String getOrderId() { return orderId; }
    public double getGbpAmount() { return gbpAmount; }
}
class LegacyCurrencyConverter {
    public double convert(double amount, String from, String to) {
        return "GBP".equalsIgnoreCase(from) && "USD".equalsIgnoreCase(to) ? amount * 1.25 : amount;
    }
}
interface PricingService { double calculateTotal(); String getReceiptBreakdown(); }
class CurrencyOrderAdapter implements PricingService {
    private final LegacyForeignOrder order; private final LegacyCurrencyConverter converter; private final String currency;
    CurrencyOrderAdapter(LegacyForeignOrder order, LegacyCurrencyConverter converter, String currency) {
        if (order == null || converter == null || currency == null) throw new IllegalArgumentException("Dependencies cannot be null");
        this.order = order; this.converter = converter; this.currency = currency;
    }
    public double calculateTotal() { return converter.convert(order.getGbpAmount(), "GBP", currency); }
    public String getReceiptBreakdown() {
        return String.format(java.util.Locale.US, "Order [%s] Base Item: $%.2f %s (Converted from %.2f GBP)", order.getOrderId(), calculateTotal(), currency, order.getGbpAmount());
    }
}
abstract class PricingDecorator implements PricingService {
    protected final PricingService wrappedService;
    PricingDecorator(PricingService wrapped) {
        if (wrapped == null) throw new IllegalArgumentException("Wrapped service cannot be null");
        wrappedService = wrapped;
    }
    public double calculateTotal() { return wrappedService.calculateTotal(); }
    public String getReceiptBreakdown() { return wrappedService.getReceiptBreakdown(); }
}
class GiftWrapDecorator extends PricingDecorator {
    GiftWrapDecorator(PricingService wrapped) { super(wrapped); }
    public double calculateTotal() { return super.calculateTotal() + 5.00; }
    public String getReceiptBreakdown() { return super.getReceiptBreakdown() + " + [Gift Wrap: $5.00]"; }
}
class ExpressCourierDecorator extends PricingDecorator {
    ExpressCourierDecorator(PricingService wrapped) { super(wrapped); }
    public double calculateTotal() { return super.calculateTotal() + 15.00; }
    public String getReceiptBreakdown() { return super.getReceiptBreakdown() + " + [Express Courier: $15.00]"; }
}
class CustomsDutyDecorator extends PricingDecorator {
    CustomsDutyDecorator(PricingService wrapped) { super(wrapped); }
    public double calculateTotal() {
        return java.math.BigDecimal.valueOf(super.calculateTotal())
                .multiply(new java.math.BigDecimal("1.085"))
                .setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }
    public String getReceiptBreakdown() { return super.getReceiptBreakdown() + " + [Customs Duty: 8.5%]"; }
}
public class Task4Runner {
    public static void main(String[] args) {
        PricingService base = new CurrencyOrderAdapter(new LegacyForeignOrder("ORD-9821", 100.00), new LegacyCurrencyConverter(), "USD");
        System.out.printf("Base Converted: $%.2f%n", base.calculateTotal());
        PricingService checkout = new CustomsDutyDecorator(new ExpressCourierDecorator(new GiftWrapDecorator(base)));
        System.out.println("\n>>> INVOICE BREAKDOWN <<<");
        System.out.println(checkout.getReceiptBreakdown());
        System.out.printf("FINAL CHARGE TOTAL: $%.2f%n", checkout.calculateTotal());
    }
}
