package local.alejandrogb.metricsserversdesktop.ui.panel.servidores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

import local.alejandrogb.metricsserversdesktop.models.Seccion;
import local.alejandrogb.metricsserversdesktop.models.Servicio;
import local.alejandrogb.metricsserversdesktop.models.servidor.Servidor;

public class ServidorTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -4282127850417899835L;

	// Imagen es la primera columna para que sea llamativa visualmente
	private static final String[] COLUMNS = { "", "ID", "Server ID", "DNS", "Hostname", "OS", "Arch", "Kernel",
			"Seccion", "Servicios" };
	/** Indice de la columna de imagen. */
	public static final int COL_IMAGEN = 0;

	private List<Servidor> data = new ArrayList<>();
	private Map<Integer, String> seccionNames = new HashMap<>();
	private Map<Integer, String> servicioNames = new HashMap<>();

	public void setData(List<Servidor> servidores, List<Seccion> secciones, List<Servicio> servicios) {

		this.data = servidores != null ? servidores : new ArrayList<>();

		seccionNames.clear();
		if (secciones != null)
			for (Seccion s : secciones)
				seccionNames.put(s.getId(), s.getNombre());

		servicioNames.clear();
		if (servicios != null)
			for (Servicio sv : servicios)
				servicioNames.put(sv.getId(), sv.getNombre());

		fireTableDataChanged();
	}

	public Servidor getRow(int row) {
		return data.get(row);
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMNS.length;
	}

	@Override
	public String getColumnName(int col) {
		return COLUMNS[col];
	}

	@Override
	public Object getValueAt(int row, int col) {
		Servidor s = data.get(row);
		return switch (col) {
		case COL_IMAGEN -> s.getImagenUrl() != null ? s.getImagenUrl() : "";
		case 1 -> s.getId();
		case 2 -> s.getServerId();
		case 3 -> s.getDns();
		case 4 -> s.getHostname();
		case 5 -> s.getPrettyOs();
		case 6 -> s.getArch();
		case 7 -> s.getKernel();
		case 8 -> resolveSeccion(s.getSeccion());
		case 9 -> resolveServicios(s.getServicios());
		default -> null;
		};
	}

	@Override
	public Class<?> getColumnClass(int c) {
		return c == 1 ? Integer.class : String.class;
	}

	private String resolveSeccion(int id) {
		if (id == 0)
			return "—";
		return seccionNames.getOrDefault(id, "Seccion #" + id);
	}

	private String resolveServicios(List<Integer> ids) {
		if (ids == null || ids.isEmpty())
			return "—";
		return ids.stream().map(id -> servicioNames.getOrDefault(id, "#" + id)).collect(Collectors.joining(", "));
	}
}
