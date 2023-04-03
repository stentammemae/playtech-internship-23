import java.io.*;
import java.util.*;

public class Analyzer {
    private static final String path = new File("").getAbsolutePath();
    public static void main(String[] args) {
        final File folder = new File(path.concat("/resources"));
        final File[] fileList = Objects.requireNonNull(folder.listFiles());
        for (File file : fileList) {
            Map<Integer, List<Turn>> sessionsByIds = new TreeMap<>();

            try (LineNumberReader br = new LineNumberReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] split = line.split(",");

                    if (split[0].equals(" ") || split[0].equalsIgnoreCase("Error")) {
                        continue;
                    }

                    int timestamp = Integer.parseInt(split[0]);
                    int sessionId = Integer.parseInt(split[1]);
                    int playerId = Integer.parseInt(split[2]);
                    String action = split[3];

                    String[] dealerHandAsArray = split[4].split("-");
                    List<Card> dealerHand = collectCards(dealerHandAsArray);

                    String[] playerHandAsArray = split[5].split("-");
                    List<Card> playerHand = collectCards(playerHandAsArray);

                    Turn turn = new Turn(
                            timestamp,
                            playerId,
                            action,
                            dealerHand,
                            playerHand
                    );

                    if (!sessionsByIds.containsKey(sessionId)) {
                        sessionsByIds.put(sessionId, new ArrayList<>());
                    }

                    sessionsByIds.get(sessionId).add(turn);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            findAllFaultyLines(sessionsByIds);
        }
    }

    private static void findAllFaultyLines(Map<Integer, List<Turn>> sessionsByIds) {
        for (Map.Entry<Integer, List<Turn>> entry : sessionsByIds.entrySet()) {
            int sessionId = entry.getKey();
            List<Turn> session = entry.getValue();

            Collections.sort(session);

            findFaultyLineBySession(session, sessionId);
        }
    }

    private static void findFaultyLineBySession(List<Turn> session, int sessionId) {
        for (int i = 0; i < session.size(); i++) {
            Turn turn = session.get(i);
            String action = turn.action();

            List<Card> dealerHand = turn.dealerHand();
            int dealerPoints = getTotalPoints(dealerHand);

            List<Card> playerHand = turn.playerHand();
            int playerPoints = getTotalPoints(playerHand);

            if (i != 0 && session.get(i - 1).action().equals("P Hit") &&
                    session.get(i - 1).playerHand().size() == playerHand.size()) {
                String line = formatTurn(session.get(i - 1), sessionId);
                addFaultyLine(line);
                break;
            }

            if (i != 0 && session.get(i - 1).action().equals("D Show") &&
                    !turn.action().equals("D Hit") && dealerPoints < 17 ||
                    action.equals("D Hit") && dealerPoints >= 17 ||
                    !action.equals("P Left") && dealerHand.size() == 0 ||
                    action.equals("P Lose") && dealerPoints > 21 ||
                    !action.equals("P Lose") && playerPoints > 21 ||
                    action.equals("P Win") && dealerPoints > playerPoints ||
                    (hasDuplicateCard(dealerHand, new HashMap<>()) || hasDuplicateCard(playerHand, new HashMap<>())) ||
                    haveDuplicateCard(dealerHand, playerHand)) {
                String line = formatTurn(turn, sessionId);
                addFaultyLine(line);
                break;
            }
        }
    }

    private static boolean haveDuplicateCard(List<Card> hand, List<Card> other) {
        Map<String, Integer> countersByCards = new HashMap<>();

        for (Card card : hand) {
            String cardAsString = card.value().toUpperCase() + card.suit();
            countersByCards.put(cardAsString, 1);
        }

        return hasDuplicateCard(other, countersByCards);
    }

    private static boolean hasDuplicateCard(List<Card> hand, Map<String, Integer> countersByCards) {
        for (Card card : hand) {
            String cardAsString = card.value().toUpperCase() + card.suit();
            Integer count = countersByCards.get(cardAsString);
            if (count == null) {
                countersByCards.put(cardAsString, 1);
            }
            else {
                return true;
            }
        }

        return false;
    }

    private static String formatTurn(Turn turn, int sessionId) {
        return turn.timestamp() + "," + sessionId + "," + turn.playerId() + "," +
                turn.action() + "," + formatHand(turn.dealerHand()) + "," + formatHand(turn.playerHand());
    }

    protected static String formatHand(List<Card> hand) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            result.append(card);
            if (i != hand.size() - 1) {
                result.append("-");
            }
        }

        if (hand.size() == 1) {
            result.append("-?");
        }

        if (hand.size() == 0) {
            result.append("?-?");
        }

        return result.toString();
    }

    private static void addFaultyLine(String line) {
        File file = new File(path.concat("/analyzer_results.txt"));

        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(file, true);
            fileWriter.write(line + "\n");
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Card> collectCards(String[] cardsAsArray) {
        List<Card> result = new ArrayList<>();

        for (String cardAsString : cardsAsArray) {
            if (cardAsString.charAt(0) != '?') {
                String value = cardAsString.substring(0, cardAsString.length() - 1);
                char suit = cardAsString.charAt(cardAsString.length() - 1);

                Card card = new Card(value, suit);
                result.add(card);
            }
        }

        return result;
    }

    private static int getTotalPoints(List<Card> hand) {
        int total = 0;

        for (Card card : hand) {
            total += card.getPoints();
        }

        return total;
    }

}