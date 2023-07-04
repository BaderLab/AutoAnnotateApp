package org.baderlab.autoannotate.internal.ui.view.create;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.util.SwingUtil;

@SuppressWarnings("serial")
public class ComboBoxCardPanel extends JPanel {
	
	public static class Card {
		final String id;
		final String label;
		
		public Card(String id, String label) {
			this.id = id;
			this.label = label;
		}
		
		@Override public String toString() {
			return label;
		}
	}
	
	
	private final List<Card> cards;
	private final Map<Card,JPanel> cardPanels = new HashMap<>();
	
	private Card currentCard;
	private JPanel cardPanel;
	
	
	public ComboBoxCardPanel(Card ... cards) {
		this.cards = Arrays.asList(cards);
		createContents();
		setOpaque(false);
	}
	
	
	private void createContents() {
		JComboBox<Card> comboBox = new JComboBox<>();
		comboBox.setEditable(false);
		SwingUtil.makeSmall(comboBox);
		
		var cardLayout = new CardLayout();
		cardPanel = new JPanel(cardLayout);
		cardPanel.setOpaque(false);
		
		for(Card card : cards) {
			JPanel contentsPanel = new JPanel();
			contentsPanel.setOpaque(false);
			comboBox.addItem(card);
			cardPanel.add(contentsPanel, card.id);
			cardPanels.put(card, contentsPanel);
		}
		
		comboBox.addItemListener((ItemEvent e) -> {
			var card = (Card) e.getItem();
			cardLayout.show(cardPanel, card.id);
			currentCard = card;
		});
		
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		add(comboBox, BorderLayout.NORTH);
		add(cardPanel, BorderLayout.CENTER);
		
		Card firstCard = cards.get(0);
		cardLayout.show(cardPanel, firstCard.id);
		currentCard = firstCard;
	}
	
	
	public void setCardContents(Card card, JPanel contents) {
		JPanel cardPanel = cardPanels.get(card);
		cardPanel.setLayout(new BorderLayout());
		cardPanel.add(contents, BorderLayout.CENTER);
	}

	
	public Card getCurrentCard() {
		return currentCard;
	}
	
}
