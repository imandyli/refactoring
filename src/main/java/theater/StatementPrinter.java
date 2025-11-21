package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private static final String COMEDY = "comedy";
    private static final String TRAGEDY = "tragedy";

    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Return the invoice associated with this printer.
     *
     * @return the invoice
     */

    public Invoice getInvoice() {
        return invoice;
    }

    /**
     * Return the plays associated with this printer.
     *
     * @return the plays map
     */

    public Map<String, Play> getPlays() {
        return plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     *
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;
        final StringBuilder result =
                new StringBuilder("Statement for " + invoice.getCustomer() + System.lineSeparator());

        final NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);

        for (final Performance performance : invoice.getPerformances()) {
            final Play play = getPlay(performance);

            final int thisAmount = getAmount(performance);
            volumeCredits += Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
            if (COMEDY.equals(play.getType())) {
                volumeCredits += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
            }
            result.append("  " + play.getName() + ": " + frmt.format(thisAmount / Constants.PERCENT_FACTOR
            ) + " (" + performance.getAudience() + " seats)" + System.lineSeparator());
            totalAmount += thisAmount;
        }
        result.append(String.format("Amount owed is %s%n", frmt.format(totalAmount / Constants.PERCENT_FACTOR)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    private int getAmount(Performance performance) {
        int result = 0;
        final Play play = getPlay(performance);
        switch (play.getType()) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON * (performance.getAudience()
                            - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON * (
                                    performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD);
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }
        return result;
    }

    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }
}
