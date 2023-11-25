import java.util.Random;
import java.util.UUID;

public class Card {
    private CardType cardType;
    private int distance;
    public UUID id;

    public Card(CardType cardType, int distance) {
        this.cardType = cardType;
        this.distance = distance;

        id = UUID.randomUUID();
    }

    public String getCardName() {
        if (cardType == CardType.DISTANCE) {
            return "+" + distance + "km";
        } else if (cardType == CardType.TROUBLE) {
            return "\033[31mRed light\033[0m";
        }
        else return "\033[32mGreen light\033[0m";
    }

    public int getDistance() {
        return distance;
    }

    public CardType getCardType() {
        return cardType;
    }
}
