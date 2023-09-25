package org.baderlab.autoannotate.internal.ui.view.create;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
	private final Map<String,JPanel> cardPanels = new HashMap<>();
	
	private Card currentCard;
	
	private JComboBox<Card> comboBox;
	private CardLayout cardLayout;
	private JPanel cardPanel;
	private JPanel bodyPanel;
	
	private List<Consumer<Card>> cardChangeListeners = new ArrayList<>();
	
	public ComboBoxCardPanel(Collection<Card> cards) {
		this.cards = new ArrayList<>(cards);
		createContents();
		setOpaque(false);
	}
	
	public ComboBoxCardPanel(Card ... cards) {
		this(Arrays.asList(cards));
	}
	
	public Card getCard(int index) {
		return cards.get(index);
	}
	
	private void createContents() {
		comboBox = new JComboBox<>();
		comboBox.setEditable(false);
		SwingUtil.makeSmall(comboBox);
		
		cardLayout = new CardLayout();
		cardPanel = new JPanel(cardLayout);
		cardPanel.setOpaque(false);
		
		for(Card card : cards) {
			JPanel contentsPanel = new JPanel();
			contentsPanel.setOpaque(false);
			comboBox.addItem(card);
			cardPanel.add(contentsPanel, card.id);
			cardPanels.put(card.id, contentsPanel);
		}
		
		comboBox.addItemListener((ItemEvent e) -> {
			var card = (Card) e.getItem();
			cardLayout.show(cardPanel, card.id);
			currentCard = card;
			fireCardChange(card);
		});
		
		bodyPanel = new JPanel(new BorderLayout());
		bodyPanel.setOpaque(false);
		bodyPanel.add(cardPanel, BorderLayout.CENTER);
		
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		add(comboBox, BorderLayout.NORTH);
		add(bodyPanel, BorderLayout.CENTER);
		
		Card firstCard = cards.get(0);
		cardLayout.show(cardPanel, firstCard.id);
		currentCard = firstCard;
	}
	
	private void fireCardChange(Card card) {
		for(var listener : cardChangeListeners) {
			listener.accept(card);
		}
	}
	
	public void setCurrentCard(Card card) {
		comboBox.setSelectedItem(card);
	}
	
	public void addCardChangeListener(Consumer<Card> listener) {
		cardChangeListeners.add(listener);
	}
	
	public void setTopContents(JPanel panel) {
		bodyPanel.add(panel, BorderLayout.NORTH);
	}
	
	public void setCardContents(Card card, JPanel contents) {
		contents.setOpaque(false);
		JPanel cardPanel = cardPanels.get(card.id);
		cardPanel.setLayout(new BorderLayout());
		cardPanel.add(contents, BorderLayout.CENTER);
	}

	
	public Card getCurrentCard() {
		return currentCard;
	}
	
}
