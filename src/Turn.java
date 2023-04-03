import java.util.List;

public record Turn(int timestamp, int playerId, String action, List<Card> dealerHand,
                   List<Card> playerHand) implements Comparable<Turn> {

    @Override
    public String toString() {
        return "[" + timestamp + ", " + action + ", " +
                Analyzer.formatHand(dealerHand) + ", " + Analyzer.formatHand(playerHand) + "]";
    }

    @Override
    public int compareTo(Turn other) {
        return Integer.compare(timestamp, other.timestamp);
    }
}
