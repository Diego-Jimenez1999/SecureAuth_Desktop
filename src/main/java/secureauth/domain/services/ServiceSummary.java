package secureauth.domain.services;

import java.util.List;

public record ServiceSummary(double serviceAmount, double productsAmount, double subtotal, double tax,
                             double discount, double total) {

    public static final double TAX_RATE = 0.19d;

    public static ServiceSummary calculate(double serviceAmount, List<ServiceProduct> products, double discount) {
        double productsAmount = products == null ? 0d : products.stream().mapToDouble(ServiceProduct::subtotal).sum();
        double subtotal = serviceAmount + productsAmount;
        double normalizedDiscount = Math.max(0d, discount);
        double taxable = Math.max(0d, subtotal - normalizedDiscount);
        double tax = taxable * TAX_RATE;
        return new ServiceSummary(serviceAmount, productsAmount, subtotal, tax, normalizedDiscount, taxable + tax);
    }
}
