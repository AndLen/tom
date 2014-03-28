package game;

import gui.CardFrame;
import gui.CardPanel;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Andrew on 28/12/13.
 */
public class CardGame {
    //Actually uses copy on write arraylists to prevent concurrent modification from threading
    //(swing vs logic)
    private List<List<Card>> board;
    private final Queue<Card> deck;
    private List<List<Card>> topRow;
    private final CardFrame cardFrame;
    private final Stack<CardMove> history;
    private long startTime;
    private int additionalHistory = 0;

    public CardGame(CardFrame cardFrame) {
        this.cardFrame = cardFrame;
        board = new CopyOnWriteArrayList<List<Card>>();
        deck = new LinkedList<Card>();
        topRow = new CopyOnWriteArrayList<List<Card>>();
        history = new Stack<CardMove>();
    }

    public void dealTestGame(CardPanel panel) {
        for (int i = 0; i < 8; i++) {
            topRow.add(new CopyOnWriteArrayList<Card>());
            for (int j = 0; j < 8; j++) {
                topRow.get(i).add((new Card(Card.Suit.values()[i % 4], Card.Rank.values()[j], true, true)));
            }

        }
        List<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < 8; i++) {
            for (int j = 12; j > 7; j--) {
                cards.add(new Card(Card.Suit.values()[i % 4], Card.Rank.values()[j], true, true));
            }
        }
        //Make 10 piles
        for (int i = 0; i < 10; i++) {
            board.add(new CopyOnWriteArrayList<Card>());
            for (int j = 0; j < 4; j++) {
                board.get(i).add(cards.remove(0));
            }
        }
        repaintWhileDealing(panel);
    }


    public void dealGame(CardPanel panel) {
        while (!panel.isReady()) {
            try {
                //Wait for graphics to render once before we deal.
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Ensures we get to see the deck being dealt
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 8; i++) {
            topRow.add(new CopyOnWriteArrayList<Card>());

        }

        List<Card> firstPack = new ArrayList<Card>();
        makePack(firstPack, false, false);
        Collections.shuffle(firstPack);

        //Make 10 piles
        for (int i = 0; i < 10; i++) {
            board.add(new CopyOnWriteArrayList<Card>());
        }
        //Deal to all 8 piles twice. Compiler will unroll anyway & easier to change
        for (int i = 0; i < 2; i++) {
            for (int j = 1; j < 9; j++) {
                //Want them all unrevealed
                board.get(j).add(firstPack.remove(0));
                repaintWhileDealing(panel);
            }
        }

        //Deal piles with upside down except last one
        for (int i = 1; i < 9; i++) {
            Card next = firstPack.remove(0);
            next.setRevealed(true);
            board.get(i).add(next);
            repaintWhileDealing(panel);
            for (int j = i + 1; j < 9; j++) {
                board.get(j).add(firstPack.remove(0));
                repaintWhileDealing(panel);

            }
        }
        List<Card> secondPack = new ArrayList<Card>();
        makePack(secondPack, true, true);
        Collections.shuffle(secondPack);
        board.get(0).add(secondPack.remove(0));
        repaintWhileDealing(panel);
        board.get(9).add(secondPack.remove(0));
        repaintWhileDealing(panel);
        deck.addAll(secondPack);
        repaintWhileDealing(panel);
    }

    private static void repaintWhileDealing(CardPanel panel, int... delays) {
        int delay = delays.length > 0 ? delays[0] : 50;
        panel.repaint();
        try {

            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void makePack(List<Card> cardList, boolean isRevealed, boolean blueBack) {
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cardList.add(new Card(suit, rank, isRevealed, blueBack));
            }
        }
    }

    public List<List<Card>> getBoard() {
        return Collections.unmodifiableList(board);
    }

    public String moveCardOntoCardFromTopRow(int boardIndexFrom, int boardIndexTo) {
        List<Card> from = topRow.get(boardIndexFrom);
        String result = moveCardOntoCard(from, boardIndexTo, from.size() - 1);
        if (result.isEmpty()) {
            from.remove(from.size() - 1);
        }
        return result;
    }

    public String moveCardOntoCardFromBoard(int toMoveTop, int boardIndexFrom, int boardIndexTo) {
        List<Card> from = board.get(boardIndexFrom);
        String result = moveCardOntoCard(from, boardIndexTo, toMoveTop);
        if (result.isEmpty()) {
            while (from.size() > toMoveTop) {
                from.remove(toMoveTop);
            }
        }
        return result;
    }

    private String moveCardOntoCard(List<Card> from, int boardIndexTo, int listIndexFrom) {
        List<Card> to = board.get(boardIndexTo);
        Card firstToMove = from.get(listIndexFrom);
        System.out.println("TO: " + to.toString());
        if (to.isEmpty()) {
            //Need to move a king there.
            if (from.get(listIndexFrom).getRank() == Card.Rank.KING) {
                for (int i = listIndexFrom; i < from.size(); i++) {
                    to.add(from.get(i));
                }
                return "";
            } else {
                return "New pile must start with a King";
            }
        }
        Card lastInRow = to.get(to.size() - 1);

        if (firstToMove == lastInRow) {
            //Don't want to move onto self, nor display warning
            return "ONTO_SELF";
        }
        if (firstToMove.getRank().ordinal() == lastInRow.getRank().ordinal() - 1) {
            if (listIndexFrom < from.size() - 1) {
                if (suitMoveIsValid(firstToMove.getSuit(), lastInRow.getSuit())) {
                    for (int i = listIndexFrom; i < from.size(); i++) {
                        to.add(from.get(i));
                    }
                    return "";
                } else {
                    return "Can only move onto a different coloured suit.";
                }

            } else {
                if (suitMoveIsValid(firstToMove.getSuit(), lastInRow.getSuit())) {
                    //Valid move
                    to.add(from.get(listIndexFrom));
                    return "";

                } else {
                    return "You can only move onto a card of different colour.";
                }
            }
        } else {
            return firstToMove.getRank().name() + " is not one lower than " + lastInRow.getRank().name();
        }


    }

    private boolean suitMoveIsValid(Card.Suit from, Card.Suit to) {
        if (from == to) {
            return false;
        }
        switch (to) {
            case HEARTS:
                return from != Card.Suit.DIAMONDS;
            case DIAMONDS:
                return from != Card.Suit.HEARTS;
            case CLUBS:
                return from != Card.Suit.SPADES;
            case SPADES:
                return from != Card.Suit.CLUBS;
            default:
                return false;
        }
    }

    public boolean isValidNewMove(int col, int index) {
        List<Card> cardList = board.get(col);
        for (int i = index + 1; i < cardList.size(); i++) {
            Card prev = cardList.get(i - 1);
            Card toCheck = cardList.get(i);
            if (!suitMoveIsValid(toCheck.getSuit(), prev.getSuit()) || toCheck.getRank().ordinal() != prev.getRank().ordinal() - 1) {
                return false;
            }
        }
        return true;
    }

    public List<List<Card>> getTopRow() {
        return Collections.unmodifiableList(topRow);
    }

    public String moveCardOntoTopRowFromRow(int boardIndexFrom, int boardIndexTo) {
        List<Card> from = topRow.get(boardIndexFrom);
        String result = moveCardOntoTopRow(from, boardIndexTo, from.size() - 1);
        if (result.isEmpty()) {
            from.remove(from.size() - 1);
        }
        return result;

    }

    public String moveCardOntoTopRowFromBoard(int toMoveTop, int boardIndexFrom, int boardIndexTo) {
        List<Card> from = board.get(boardIndexFrom);
        String result = moveCardOntoTopRow(from, boardIndexTo, toMoveTop);
        if (result.isEmpty()) {
            from.remove(from.size() - 1);
        }
        return result;
    }

    private String moveCardOntoTopRow(List<Card> from, int boardIndexTo, int listIndexFrom) {

        List<Card> pile = topRow.get(boardIndexTo);
        Card fromTop = from.get(listIndexFrom);
        if (pile.size() == 0) {
            if (fromTop.getRank() == Card.Rank.ACE) {
                pile.add(from.get(listIndexFrom));
                return "";
            } else {
                return "Can only move an Ace to an empty pile.";
            }
        } else {
            Card pileTop = pile.get(pile.size() - 1);
            //Want reference equality
            if (pileTop == fromTop) {
                //Don't want to move onto self, nor display warning
                return "ONTO_SELF";
            }
            if (listIndexFrom != from.size() - 1) {
                return "Can only move the bottom card to the top row.";
            }
            if (fromTop.getSuit().ordinal() == pileTop.getSuit().ordinal()) {
                if (fromTop.getRank().ordinal() == pileTop.getRank().ordinal() + 1) {
                    pile.add(from.get(listIndexFrom));
                    return "";
                } else {
                    return fromTop.getRank().name() + " is not one higher than " + pileTop.getRank().name();
                }

            } else {
                return "Can only move to a pile of the same suit";
            }
        }
    }

    public Queue<Card> getDeck() {
        return deck;
    }

    public boolean hasWon(List<List<Card>> topRowCards) {

        for (List<Card> pile : topRowCards) {
            if (pile.size() != 13) {
                return false;
            }
        }
        return true;
    }

    public void restart() {
        cardFrame.restartGame();
    }

    public Stack<CardMove> getHistory() {
        return history;
    }

    public void undo() {
        System.out.println(history);
        if (!history.isEmpty()) {
            CardMove toUndo = history.pop();
            toUndo.undo(this);

        }

    }

    public long getStartTime() {
        return startTime;
    }

    public int getNumMoves() {
        //Handy
        return history.size() + additionalHistory;
    }

    public String dealDeck(final CardPanel panel) {
        //Have to thread it as we can't hope to repaint on the EDT

        Thread t = new Thread() {
            public void run() {
                synchronized (CardGame.this) {
                    for (int i = 0; i < 10 && !deck.isEmpty(); i++) {
                        board.get(i).add(deck.poll());
                        repaintWhileDealing(panel);
                    }
                }
            }
        };
        t.start();
        return "";
    }

    public String reveal(int boardIndexFrom, int toMoveTop) {
        board.get(boardIndexFrom).get(toMoveTop).setRevealed(true);
        return "";
    }

    public boolean tryFinish(CardPanel panel) {
        class FinishException extends Exception {
        }

        List<List<Card>> localTopRow = new ArrayList<List<Card>>(topRow);
        List<List<Card>> localBoard = new ArrayList<List<Card>>(board);
        //Lets clear up the pesky bits first - we can't do this if anything is not revealed
        try {
            for (List<Card> cards : board) {
                for (Card card : cards) {
                    if (!card.isRevealed()) {
                        throw new FinishException();
                    }
                }

            }
            //Cool, now everything is revealed. This simplifies the logic.
            while (!hasWon(topRow)) {
                boolean changedThisLoop = false;
                for (int i = 0; i < topRow.size(); i++) {
                    for (int j = 0; j < board.size(); j++) {
                        List<Card> cards = board.get(j);
                        if (cards.size() != 0) {
                            String s = moveCardOntoTopRowFromBoard(cards.size() - 1, j, i);
                            if (s.isEmpty()) {
                                repaintWhileDealing(panel, 200);
                                changedThisLoop = true;
                                additionalHistory++;
                            }
                        }

                    }
                }
                //We haven't won and yet haven't changed anything - must've been unable to do it
                if (!changedThisLoop) {
                    throw new FinishException();
                }
            }
            //yay
            return true;
        } catch (FinishException finishException) {
            topRow = localTopRow;
            board = localBoard;
            return false;
        }
    }
}
