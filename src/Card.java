public record Card(String value, char suit) {

    int getPoints() {
        if (value.equalsIgnoreCase("J") ||
                value.equalsIgnoreCase("Q") ||
                value.equalsIgnoreCase("K")) {
            return 10;
        } else if (value.equalsIgnoreCase("A")) {
            return 11;
        } else {
            return Integer.parseInt(value);
        }
    }

    @Override
    public String toString() {
        return String.format("%s%s", value, suit);
    }
}
