import java.util.ArrayList;
import java.awt.event.*;
import java.awt.*;
import java.util.Random;

import javax.management.ListenerNotFoundException;
import javax.swing.*;

public class BlackJack  {

    private class Card {
        String value;
        String type;

        Card(String value, String type){
            this.value = value;
            this.type = type;
        }

        @Override
        public String toString(){
            return value + "-" + type;
        }

        public int getValue(){
            //return value of Ace, Jack, Queen or King
            if ("A,J,Q,K".contains(value)){
                if (value == "A"){
                    return 11;
                }
                else {
                    return 10;
                }
            }
            //return card values from 2 to 10
            return Integer.parseInt(value);
        }

        public boolean isAce(){
            return value == "A";
        }

        public String getImagePath(){
            return "./cards/" + toString() + ".png";
        }
    }

    ArrayList<Card> deck;
    Random  random = new Random();

    //dealer's hand
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerTotal;
    int dealerAceCount;

    //player's hand
    ArrayList<Card> playerHand;
    int playerTotal;
    int playerAceCount;

    //window initialization
    int windowWidth = 800;
    int windowHeight = windowWidth;

    int cardHeight = 210; 
    int cardWidth = 150;    //ratio should be 1:1.4

    JFrame jFrame = new JFrame("Black Jack");
    JPanel gamePanel = new JPanel(){
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);

            try {
                //draw hidden card
                Image hiddenCardImage = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                if (!stayButton.isEnabled()) {
                    hiddenCardImage = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                }
                g.drawImage((hiddenCardImage),20, 20, cardWidth, cardHeight, null);

                //draw dealer's hand
                for (int i = 0; i < dealerHand.size(); i++){
                    Card card = dealerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, cardWidth + 25 + (cardWidth + 5) * i , 20, cardWidth, cardHeight, null);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.PLAIN, 30));
                    g.drawString("Dealer", 20, 300);
                }

                //draw player's hand
                for (int i = 0; i < playerHand.size(); i++){
                    Card card = playerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg,  25 + (cardWidth + 5) * i, 480, cardWidth, cardHeight, null);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.PLAIN, 30));
                    g.drawString("Player", 20, 420);
                }

                if (!stayButton.isEnabled()){
                    dealerTotal = reduceAceDealer();
                    playerTotal = reduceAcePlayer();
                    System.out.println("Stay: ");
                    System.out.println(dealerTotal);
                    System.out.println(playerTotal);

                    String msg ="";
                    if (playerTotal > 21){
                        msg = "You Lose!";
                    } else if (dealerTotal > 21){
                        msg = "You Win!";
                    } else if (playerTotal == dealerTotal){
                        msg = "It's a Tie!";
                    } else if (playerTotal > dealerTotal){
                        msg = "You Win!";
                    } else if (playerTotal < dealerTotal){
                        msg = "You Lose!";
                    }

                    g.setFont(new Font("Arial", Font.PLAIN, 40));
                    g.setColor(Color.WHITE);
                    g.drawString(msg, 300, 350);
                }
            } catch (Exception e){
                e.printStackTrace();

            }
            
        }
    };
    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");

    BlackJack(){
        startGame();

        jFrame.setVisible(true);
        jFrame.setSize(windowWidth, windowHeight);
        jFrame.setLocationRelativeTo(null);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(53, 101, 77));
        jFrame.add(gamePanel);

        hitButton.setFocusable(false);
        hitButton.setBackground(Color.BLACK);
        hitButton.setForeground(Color.WHITE);
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        stayButton.setBackground(Color.BLACK);
        stayButton.setForeground(Color.WHITE);
        buttonPanel.add(stayButton);
        jFrame.add(buttonPanel, BorderLayout.SOUTH);

        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Card card = deck.remove(deck.size() - 1);
                playerTotal += card.getValue();
                playerAceCount += card.isAce() ? 1 : 0;
                playerHand.add(card);
                if (reduceAcePlayer() > 21){
                    hitButton.setEnabled((false));
                }
                gamePanel.repaint();
            }
        });

        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);

                while (dealerTotal < 17){
                    Card card = deck.remove(deck.size() - 1);
                    dealerTotal += card.getValue();
                    dealerAceCount += card.isAce() ? 1 : 0;
                    dealerHand.add(card);
                }
                gamePanel.repaint();
            }
        });

        gamePanel.repaint();
    }

    public void startGame(){
        // build the deck of cards
        buildDeck();
        // shuffle the deck before starting game
        shuffleDeck();

        //initialize dealer
        dealerHand = new ArrayList<Card>();
        dealerTotal = 0;
        dealerAceCount = 0;

        //remove card at last index and get hidden card
        hiddenCard = deck.remove(deck.size() - 1); 
        dealerTotal += hiddenCard.getValue();           //update total
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;   //check if card is an ace
        
        //get second card for dealer
        Card card = deck.remove(deck.size() - 1);
        dealerTotal += card.getValue();                 //update total
        dealerAceCount += card.isAce() ? 1 : 0;         //check if card is an ace
        dealerHand.add(card);                           //add card to dealer's hand

        System.out.println("Dealer's Hand:");
        System.out.println(hiddenCard);
        System.out.println(dealerHand);
        System.out.println(dealerTotal);
        System.out.println(dealerAceCount);

        //initialize player
        playerHand = new ArrayList<Card>();
        playerTotal = 0;
        playerAceCount = 0;

        for (int i = 0; i < 2; i++){
            card = deck.remove(deck.size() - 1);
            playerTotal += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
        }

        System.out.println("Player:");
        System.out.println(playerHand);
        System.out.println(playerTotal);
        System.out.println(playerAceCount);
    }
    //function to build deck of cards
    public void buildDeck(){
        //creating deck and cards
        deck = new ArrayList<Card>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        //adding cards to deck
        for (int i = 0; i < types.length; i++){
            for (int j = 0; j < values.length; j++){
                Card card = new Card(values[j], types[i]);
                deck.add(card);
            }
        }

        System.out.println("Build Deck:");
        System.out.println(deck);
    }

    public void shuffleDeck(){
        for (int i = 0; i < deck.size(); i ++){
            int j = random.nextInt(deck.size());
            Card currentCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currentCard);
        }

        System.out.println("After Shuffling:");
        System.out.println(deck);
    }

    public int reduceAcePlayer() {
        while (playerTotal > 21 && playerAceCount > 1){
            playerTotal -= 10;
            playerAceCount -= 1;
        }
        return playerTotal;
    }

    public int reduceAceDealer(){
        while (dealerTotal > 21 && dealerAceCount > 0){
            dealerTotal -= 10;
            dealerAceCount -= 1;
        }
        return dealerTotal;
    }
}
