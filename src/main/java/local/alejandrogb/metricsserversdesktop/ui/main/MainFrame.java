package local.alejandrogb.metricsserversdesktop.ui.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import local.alejandrogb.metricsserversdesktop.services.auth.AuthService;
import local.alejandrogb.metricsserversdesktop.services.permission.PermissionGuard;
import local.alejandrogb.metricsserversdesktop.ui.component.BaseTablePanel;
import local.alejandrogb.metricsserversdesktop.ui.login.LoginFrame;
import local.alejandrogb.metricsserversdesktop.ui.panel.ambitos.AmbitosPanel;
import local.alejandrogb.metricsserversdesktop.ui.panel.grupos.GruposPanel;
import local.alejandrogb.metricsserversdesktop.ui.panel.permisos.PermisosPanel;
import local.alejandrogb.metricsserversdesktop.ui.panel.secciones.SeccionesPanel;
import local.alejandrogb.metricsserversdesktop.ui.panel.servicios.ServiciosPanel;
import local.alejandrogb.metricsserversdesktop.ui.panel.servidores.ServidoresPanel;
import local.alejandrogb.metricsserversdesktop.ui.util.SwingUtils;

/**
 * Ventana principal de la aplicación.
 * <p>
 * Estructura:
 * <ul>
 * <li>Norte: {@link HeaderPanel} con datos del usuario</li>
 * <li>Centro: panel con {@link CardLayout} para las vistas</li>
 * <li>MenuBar: navegación entre vistas, condicionada por permisos</li>
 * </ul>
 */
public class MainFrame extends JFrame {

	private static final long serialVersionUID = -8672901514278680423L;
	private static final String CARD_SERVIDORES = "servidores";
	private static final String CARD_SECCIONES = "secciones";
	private static final String CARD_SERVICIOS = "servicios";
	private static final String CARD_GRUPOS = "grupos";
	private static final String CARD_PERMISOS = "permisos";
	private static final String CARD_AMBITOS = "ambitos";

	private final CardLayout cardLayout = new CardLayout();
	private final JPanel cardPanel = new JPanel(cardLayout);

	/** Mapa nombre → panel para poder hacer refresh al cambiar de vista. */
	private final Map<String, BaseTablePanel<?>> panels = new LinkedHashMap<>();

	private String currentCard = null;

	public MainFrame() {
		super("Metrics Manager");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setMinimumSize(new Dimension(900, 600));
		setPreferredSize(new Dimension(1200, 750));

		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				confirmExit();
			}
		});

		buildUI();
		pack();
		SwingUtils.centerOn(this, null);
	}

	private void buildUI() {
		// ── Header ────────────────────────────────────────────────────────
		add(new HeaderPanel(), BorderLayout.NORTH);

		// ── Card panel ────────────────────────────────────────────────────
		PermissionGuard guard = PermissionGuard.getInstance();

		if (guard.canViewServidores()) {
			ServidoresPanel p = new ServidoresPanel();
			cardPanel.add(p, CARD_SERVIDORES);
			panels.put(CARD_SERVIDORES, p);
		}
		if (guard.canManageSecciones() || guard.isSuperAdmin()) {
			SeccionesPanel p = new SeccionesPanel();
			cardPanel.add(p, CARD_SECCIONES);
			panels.put(CARD_SECCIONES, p);
		}
		if (guard.canManageServicios() || guard.isSuperAdmin()) {
			ServiciosPanel p = new ServiciosPanel();
			cardPanel.add(p, CARD_SERVICIOS);
			panels.put(CARD_SERVICIOS, p);
		}
		if (guard.canViewUsuarios()) {
			GruposPanel p = new GruposPanel();
			cardPanel.add(p, CARD_GRUPOS);
			panels.put(CARD_GRUPOS, p);
		}

		// GET /permisos requiere AUDIT_USER
		if (guard.canViewUsuarios()) {
			PermisosPanel pp = new PermisosPanel();
			cardPanel.add(pp, CARD_PERMISOS);
			panels.put(CARD_PERMISOS, pp);
		}
		// GET /ambitos requiere AUDIT_SYS
		if (guard.canViewAmbitos()) {
			AmbitosPanel ap = new AmbitosPanel();
			cardPanel.add(ap, CARD_AMBITOS);
			panels.put(CARD_AMBITOS, ap);
		}

		add(cardPanel, BorderLayout.CENTER);

		// ── MenuBar ───────────────────────────────────────────────────────
		setJMenuBar(buildMenuBar());

		// Mostrar primer panel disponible y cargar datos
		if (!panels.isEmpty()) {
			String first = panels.keySet().iterator().next();
			showPanel(first);
		}
	}

	private JMenuBar buildMenuBar() {
		JMenuBar bar = new JMenuBar();
		//PermissionGuard guard = PermissionGuard.getInstance();

		// ── Menú Gestión ──────────────────────────────────────────────────
		JMenu menuGestion = new JMenu("Gestión");
		menuGestion.setMnemonic('G');

		if (panels.containsKey(CARD_SERVIDORES)) {
			menuGestion.add(menuItem("Servidores", CARD_SERVIDORES));
		}
		if (panels.containsKey(CARD_SECCIONES)) {
			menuGestion.add(menuItem("Secciones", CARD_SECCIONES));
		}
		if (panels.containsKey(CARD_SERVICIOS)) {
			menuGestion.add(menuItem("Servicios", CARD_SERVICIOS));
		}
		if (panels.containsKey(CARD_GRUPOS)) {
			menuGestion.add(menuItem("Grupos", CARD_GRUPOS));
		}

		if (menuGestion.getMenuComponentCount() > 0) {
			bar.add(menuGestion);
		}

		// ── Menú Configuración ────────────────────────────────────────────
		JMenu menuConfig = new JMenu("Configuración");
		menuConfig.setMnemonic('C');
		if (panels.containsKey(CARD_PERMISOS)) {
			menuConfig.add(menuItem("Permisos", CARD_PERMISOS));
		}
		if (panels.containsKey(CARD_AMBITOS)) {
			menuConfig.add(menuItem("Ámbitos", CARD_AMBITOS));
		}
		if (menuConfig.getMenuComponentCount() > 0) {
			bar.add(menuConfig);
		}

		// ── Menú Sesión ───────────────────────────────────────────────────
		JMenu menuSesion = new JMenu("Sesión");
		menuSesion.setMnemonic('S');

		JMenuItem miRefresh = new JMenuItem("🔄 Actualizar vista");
		miRefresh.addActionListener(e -> refreshCurrent());
		menuSesion.add(miRefresh);

		menuSesion.addSeparator();

		JMenuItem miLogout = new JMenuItem("Cerrar sesión");
		miLogout.addActionListener(e -> logout());
		menuSesion.add(miLogout);

		JMenuItem miExit = new JMenuItem("Salir");
		miExit.addActionListener(e -> confirmExit());
		menuSesion.add(miExit);

		bar.add(menuSesion);

		return bar;
	}

	private JMenuItem menuItem(String text, String card) {
		JMenuItem mi = new JMenuItem(text);
		mi.addActionListener(e -> showPanel(card));
		return mi;
	}

	private void showPanel(String card) {
		cardLayout.show(cardPanel, card);
		currentCard = card;
		BaseTablePanel<?> p = panels.get(card);
		if (p != null)
			p.refresh();
	}

	private void refreshCurrent() {
		if (currentCard != null) {
			BaseTablePanel<?> p = panels.get(currentCard);
			if (p != null)
				p.refresh();
		}
	}

	private void logout() {
		if (!SwingUtils.confirm(this, "¿Cerrar sesión y volver al login?"))
			return;
		new AuthService().logout();
		dispose();
		SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
	}

	private void confirmExit() {
		if (SwingUtils.confirm(this, "¿Salir de la aplicación?")) {
			System.exit(0);
		}
	}
}
