import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private static Player player;
    private static Scanner scanner = new Scanner(System.in);
    private static Player opponent;
    public static void main(String[] args) {
        startGame();
    }

    public static void startGame() {
        player = new Player();
        opponent = new Player();

        drawInitialHand(player);
        drawInitialHand(opponent);

        System.out.print("Enter your name: ");
        String playerName = scanner.nextLine();

        for (; true ;) {
            renderPlayerStats(opponent, "BOT");
            renderPlayerStats(player, playerName);
            renderPlayerCards(player);
            System.out.print("Select card: ");
            String input = scanner.nextLine();
            System.out.print("You selected ");

            if (input.equals("")) continue;

            Card selectedCard = getCardByID(player.getHand(), Integer.parseInt(input)-1);
            System.out.println(selectedCard.getCardName());

            Card lastPlacedCard = player.getPlacedCards().getLast();
            Card lastOpponentsCard = opponent.getPlacedCards().getLast();
            boolean canAttackOpponent = lastOpponentsCard.getCardType() == CardType.HELP;
            boolean haveToHelpSelf = lastPlacedCard.getCardType() == CardType.TROUBLE;

            boolean turnMade = false;

            if (selectedCard.getCardType() == CardType.TROUBLE) {
                if (canAttackOpponent) {
                    System.out.println("Are you sure you want stop your opponent (type y to accept, type any to cancel)");
                    String shouldAttack = scanner.nextLine();

                    if (shouldAttack.equals("y")) {
                        opponent.placeCard(selectedCard);
                        player.removeFromHand(selectedCard);
                        player.addToHand(drawCard());
                        turnMade = true;
                    }
                } else {
                    System.out.println("You want to remove your card? (type y to accept, type any to cancel)");
                    String shouldRemove = scanner.nextLine();

                    if (shouldRemove.equals("y")) {
                        player.removeFromHand(selectedCard);
                        player.addToHand(drawCard());
                        turnMade = true;
                    }
                }
            }

            if (selectedCard.getCardType() == CardType.HELP) {
                if (haveToHelpSelf) {
                    System.out.println("Are you sure you want to use help card?");
                    String shouldHelp = scanner.nextLine();

                    if (shouldHelp.equals("y")) {
                        player.placeCard(selectedCard);
                        player.removeFromHand(selectedCard);
                        player.addToHand(drawCard());
                        turnMade = true;
                    }
                } else {
                    System.out.println("You want to remove your card? (type y to accept, type any to cancel)");
                    String shouldRemove = scanner.nextLine();

                    if (shouldRemove.equals("y")) {
                        player.removeFromHand(selectedCard);
                        player.addToHand(drawCard());
                        turnMade = true;
                    }
                }
            }

            if (selectedCard.getCardType() == CardType.DISTANCE) {
                if (!haveToHelpSelf) {
                    System.out.println("Are you sure you want to use distance card?");
                    String shouldHelp = scanner.nextLine();

                    if (shouldHelp.equals("y")) {
                        player.addToDistanceStack(selectedCard);
                        player.removeFromHand(selectedCard);
                        player.addToHand(drawCard());
                        turnMade = true;
                    }
                } else {
                    System.out.println("You have to use green light first");
                    System.out.println("You want to remove your card? (type y to accept, type any to cancel)");
                    String shouldRemove = scanner.nextLine();

                    if (shouldRemove.equals("y")) {
                        player.removeFromHand(selectedCard);
                        player.addToHand(drawCard());
                        turnMade = true;
                    }
                }
            }

            if (turnMade) {
                makeTurnBot(player, opponent);
            }
        }
    }

    private static void makeTurnBot(Player player, Player opponent) {
        Card lastPlayerCard = player.getPlacedCards().getLast();
        Card lastBotCard = opponent.getPlacedCards().getLast();
        boolean canAttackPlayer = lastPlayerCard.getCardType() == CardType.HELP;
        boolean haveToHelpSelf = lastBotCard.getCardType() == CardType.TROUBLE;


        List<Card> helpCards = opponent.getHand().stream()
                .filter(card -> card.getCardType() == CardType.HELP)
                .collect(Collectors.toList());

        List<Card> atackCards = opponent.getHand().stream()
                .filter(card -> card.getCardType() == CardType.TROUBLE)
                .collect(Collectors.toList());

        List<Card> distanceCards = opponent.getHand().stream()
                .filter(card -> card.getCardType() == CardType.DISTANCE)
                .collect(Collectors.toList());

        Card helpCard = helpCards.size() > 0? helpCards.get(0) : null;
        Card atackCard = atackCards.size() > 0? atackCards.get(0) : null;
        Card distanceCard = atackCards.size() > 0? distanceCards.get(0) : null;

        Random random = new Random();
        int randomNumber = random.nextInt(2);

        if (randomNumber == 1) {
            atackCard = null;
        }


        if (haveToHelpSelf && helpCard != null) {
            opponent.placeCard(helpCard);
            opponent.removeFromHand(helpCard);
            System.out.println("BOT used Green light");
        }
        else if (canAttackPlayer && atackCard != null) {
            player.placeCard(atackCard);
            opponent.removeFromHand(atackCard);
            System.out.println("BOT stopped you");
        }
        else if (!haveToHelpSelf && distanceCard != null) {
            opponent.addToDistanceStack(distanceCard);
            opponent.removeFromHand(distanceCard);
            System.out.println("BOT drove " + distanceCard.getDistance() + "km more");
        }

        else {
            opponent.removeFromHand(opponent.getHand().get(0));
        }

        opponent.addToHand(drawCard());
    }


    private static void renderPlayerStats(Player player, String name) {
        List<Card> distanceStack = player.getDistanceStack();
        List<Card> placedStack = player.getPlacedCards();

        int totalDistance = 0;

        for (int i = 0; i < distanceStack.size(); i++) {
            Card card = distanceStack.get(i);
            totalDistance += card.getDistance();
        }

        if (totalDistance > 1000) {
            System.out.println("Game finished! " +  name + " is the winner");
        }

        System.out.print("\nDrove " + totalDistance + " km");

        Card lastPlacedCard = placedStack.getLast();

        String status = "";
        CardType type = lastPlacedCard.getCardType();

        if (type == CardType.TROUBLE) status = "\033[31m" + name + " must place a card with a green light\033[0m";
        if (type == CardType.HELP) status = "\033[32m" + name + " can go\033[0m";

        System.out.print(" " + status + "\n");
    }

    private static Card getCardByID(List<Card> stack, int idx) {
        return stack.get(idx);
    }
    private static void renderPlayerCards(Player player) {
        List<Card> hand = player.getHand();

        System.out.println("Your cards: ");

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);

            System.out.println( i+1 + ". " + card.getCardName());
        }


    }
    private static void drawInitialHand(Player player) {
        for (int i = 0; i < 6; i++) {
            Card randomCard = drawCard();
            player.addToHand(randomCard);
        }
    }

    public static CardType getRandomCardType() {
        CardType[] cardTypes = CardType.values();
        int randomIndex = new Random().nextInt(cardTypes.length);
        return cardTypes[randomIndex];
    }

    public static int getRandomDistance() {
        int[] distances = { 25, 50, 75, 100, 200 };
        int randomIndex = new Random().nextInt(distances.length);
        return distances[randomIndex];
    }


    private static Card drawCard() {
        CardType randomType = getRandomCardType();
        int distance =  getRandomDistance();

        Card card;
        if (CardType.DISTANCE == randomType) {
            card = new Card(CardType.DISTANCE, distance);
        } else {
            card = new Card(randomType, 0);
        }
        return card;
    }
}