package local.alejandrogb.metricsserversdesktop.ui.util;

import java.awt.*;

/**
 * Paleta de colores de la aplicación. FlatLaf maneja light/dark
 * automáticamente, pero aquí definimos los colores de acento y estado que
 * usamos de forma explícita.
 */
public final class AppColors {

	private AppColors() {
	}

	public static final Color PRIMARY = new Color(0x2C7BE5);
	public static final Color PRIMARY_DARK = new Color(0x1A5CB5);
	public static final Color SUCCESS = new Color(0x00C17A);
	public static final Color WARNING = new Color(0xF5A623);
	public static final Color DANGER = new Color(0xE53935);

	public static final Color HEADER_BG = new Color(0x1E2A3A);
	public static final Color HEADER_FG = Color.WHITE;

	public static final Color SUPERADMIN_BADGE = new Color(0xFF6B35);
	public static final Color SUPERADMIN_FG = Color.WHITE;

	public static final Color ROW_EVEN = new Color(0xF8F9FA);
	public static final Color ROW_ODD = Color.WHITE;
	public static final Color ROW_SELECTED = new Color(0xBDD7F5);

	public static final Color SEPARATOR = new Color(0xDEE2E6);
}
