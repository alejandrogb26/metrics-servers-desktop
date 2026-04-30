package local.alejandrogb.metricsserversdesktop.ui.component;

import javax.swing.*;
import java.awt.*;

/**
 * Panel superpuesto semitransparente con un spinner y un mensaje. Se usa para
 * bloquear visualmente la UI durante operaciones de red.
 */
public class LoadingPanel extends JPanel {

	private static final long serialVersionUID = 4924644495677008522L;
	private final JLabel messageLabel;

	public LoadingPanel() {
		setLayout(new GridBagLayout());
		setOpaque(false);
		setBackground(new Color(0, 0, 0, 80));

		JPanel card = new JPanel(new GridBagLayout());
		card.setOpaque(true);
		card.setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));
		card.setBackground(UIManager.getColor("Panel.background"));

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(6, 6, 6, 6);
		c.gridx = 0;
		c.gridy = 0;

		JProgressBar spinner = new JProgressBar();
		spinner.setIndeterminate(true);
		spinner.setPreferredSize(new Dimension(200, 8));
		card.add(spinner, c);

		c.gridy = 1;
		messageLabel = new JLabel("Cargando…");
		messageLabel.setFont(messageLabel.getFont().deriveFont(13f));
		card.add(messageLabel, c);

		add(card);
	}

	public void setMessage(String msg) {
		messageLabel.setText(msg);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(new Color(0, 0, 0, 80));
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.dispose();
		super.paintComponent(g);
	}
}
