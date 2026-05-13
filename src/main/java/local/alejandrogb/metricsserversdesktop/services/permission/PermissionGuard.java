package local.alejandrogb.metricsserversdesktop.services.permission;

import java.util.List;
import java.util.Map;

import local.alejandrogb.metricsserversdesktop.models.Grupo;
import local.alejandrogb.metricsserversdesktop.models.PermissionMap;
import local.alejandrogb.metricsserversdesktop.models.Session;
import local.alejandrogb.metricsserversdesktop.services.auth.SessionHolder;

/**
 * Centraliza toda la lógica de control de acceso de la aplicación.
 *
 * <p>
 * Las claves de permiso que llegan de la API tienen el formato
 * {@code NOMBRE_PERMISO_NOMBRE_AMBITO}, construidas por la query de la API:
 * {@code CONCAT(p.nombre, '_', a.nombre)}.
 * </p>
 *
 * <p>
 * Ejemplos de claves reales: {@code MODIFY_USER}, {@code MODIFY_SERV},
 * {@code MODIFY_SYS}, {@code AUDIT_USER}, {@code AUDIT_SERV},
 * {@code AUDIT_SYS}.
 * </p>
 *
 * <p>
 * Reglas de evaluación (en orden):
 * <ol>
 * <li>Si el usuario es {@code superadmin} → acceso total.</li>
 * <li>Clave global ({@code permisos.global}) → aplica a todas las entidades del
 * ámbito correspondiente.</li>
 * <li>Clave por sección ({@code permisos.sections}) → solo para los
 * servidores/recursos de esa sección concreta.</li>
 * </ol>
 * </p>
 */
public class PermissionGuard {

	// ── Claves compuestas PERMISO_AMBITO ──────────────────────────────────

	/** Puede crear/editar/borrar servidores (ámbito SERV). */
	public static final String MODIFY_SERV = "MODIFY_SERV";

	/** Puede crear/editar/borrar usuarios/grupos (ámbito USER). */
	public static final String MODIFY_USER = "MODIFY_USER";

	/** Puede crear/editar/borrar recursos de sistema (ámbito SYS). */
	public static final String MODIFY_SYS = "MODIFY_SYS";

	/** Puede consultar/auditar servidores (ámbito SERV). */
	public static final String AUDIT_SERV = "AUDIT_SERV";

	/** Puede consultar/auditar usuarios/grupos (ámbito USER). */
	public static final String AUDIT_USER = "AUDIT_USER";

	/** Puede consultar/auditar recursos de sistema (ámbito SYS). */
	public static final String AUDIT_SYS = "AUDIT_SYS";

	// ── Singleton ─────────────────────────────────────────────────────────

	private static final PermissionGuard INSTANCE = new PermissionGuard();

	private PermissionGuard() {
	}

	public static PermissionGuard getInstance() {
		return INSTANCE;
	}

	// ── Comprobaciones de alto nivel ──────────────────────────────────────

	/**
	 * ¿Puede gestionar (crear/editar/borrar) servidores?
	 * <p>
	 * Con {@code seccionId == 0} comprueba si tiene la clave en alguna sección o de
	 * forma global. Con un {@code seccionId} concreto comprueba solo esa sección.
	 */
	public boolean canManageServidores(int seccionId) {
		if (isSuperAdmin())
			return true;
		if (hasGlobalKey(MODIFY_SERV))
			return true;
		if (seccionId == 0)
			return hasAnyServKey(MODIFY_SERV);
		return hasSectionKey(MODIFY_SERV, seccionId);
	}

	/**
	 * ¿Puede ver (auditar) servidores?
	 * <p>
	 * Con {@code seccionId == 0} comprueba cualquier sección o permiso global.
	 */
	public boolean canAuditServidores(int seccionId) {
		if (isSuperAdmin())
			return true;
		if (hasGlobalKey(AUDIT_SERV) || hasGlobalKey(MODIFY_SERV))
			return true;
		if (seccionId == 0)
			return hasAnyServKey(AUDIT_SERV) || hasAnyServKey(MODIFY_SERV);
		return hasSectionKey(AUDIT_SERV, seccionId) || hasSectionKey(MODIFY_SERV, seccionId);
	}

	/** ¿Puede ver servidores en general (sin restringir a sección)? */
	public boolean canViewServidores() {
		return isSuperAdmin() || hasGlobalKey(MODIFY_SERV) || hasGlobalKey(AUDIT_SERV) || hasAnyServKey(MODIFY_SERV)
				|| hasAnyServKey(AUDIT_SERV);
	}

	/** ¿Puede gestionar usuarios/grupos? */
	public boolean canManageUsuarios() {
		return isSuperAdmin() || hasGlobalKey(MODIFY_USER);
	}

	/** ¿Puede ver usuarios/grupos? */
	public boolean canViewUsuarios() {
		return isSuperAdmin() || hasGlobalKey(MODIFY_USER) || hasGlobalKey(AUDIT_USER);
	}

	/** ¿Puede gestionar secciones? (requiere MODIFY_SERV o MODIFY_SYS global) */
	public boolean canManageSecciones() {
		return isSuperAdmin() || hasGlobalKey(MODIFY_SERV) || hasGlobalKey(MODIFY_SYS);
	}

	/** ¿Puede gestionar servicios? (requiere MODIFY_SERV o MODIFY_SYS global) */
	public boolean canManageServicios() {
		return isSuperAdmin() || hasGlobalKey(MODIFY_SERV) || hasGlobalKey(MODIFY_SYS);
	}

	/** ¿Puede ver ámbitos? GET /ambitos requiere AUDIT_SYS en api-py. */
	public boolean canViewAmbitos() {
		return isSuperAdmin() || hasGlobalKey(AUDIT_SYS) || hasGlobalKey(MODIFY_SYS);
	}

	// ── Primitivas de clave compuesta ─────────────────────────────────────

	public boolean isSuperAdmin() {
		Session s = session();
		if (s == null)
			return false;
		Grupo g = s.getGrupo();
		return g != null && Boolean.TRUE.equals(g.isSuperAdmin());
	}

	/**
	 * Comprueba si la clave compuesta aparece en la lista global del usuario.
	 * La API devuelve los permisos como strings, por lo que la comparación es directa.
	 *
	 * @param key clave compuesta (e.g. {@code "MODIFY_SERV"})
	 */
	public boolean hasGlobalKey(String key) {
		if (isSuperAdmin())
			return true;
		PermissionMap pm = permissionMap();
		if (pm == null || pm.getGlobal() == null)
			return false;
		return pm.getGlobal().contains(key);
	}

	/**
	 * Comprueba si la clave compuesta aparece en los permisos de una sección concreta.
	 *
	 * @param key       clave compuesta (e.g. {@code "MODIFY_SERV"})
	 * @param seccionId ID de la sección
	 */
	public boolean hasSectionKey(String key, int seccionId) {
		if (isSuperAdmin())
			return true;
		if (hasGlobalKey(key))
			return true;
		PermissionMap pm = permissionMap();
		if (pm == null || pm.getSections() == null)
			return false;
		List<String> sectionKeys = pm.getSections().get(seccionId);
		return sectionKeys != null && sectionKeys.contains(key);
	}

	/**
	 * Comprueba si la clave compuesta aparece en los permisos de ALGUNA sección.
	 *
	 * @param key clave compuesta (e.g. {@code "AUDIT_SERV"})
	 */
	public boolean hasAnyServKey(String key) {
		if (isSuperAdmin())
			return true;
		if (hasGlobalKey(key))
			return true;
		PermissionMap pm = permissionMap();
		if (pm == null || pm.getSections() == null)
			return false;
		for (Map.Entry<Integer, List<String>> entry : pm.getSections().entrySet()) {
			if (entry.getValue() != null && entry.getValue().contains(key))
				return true;
		}
		return false;
	}

	// ── Helpers ───────────────────────────────────────────────────────────

	private Session session() {
		return SessionHolder.getInstance().getSession();
	}

	private PermissionMap permissionMap() {
		Session s = session();
		return s != null ? s.getPermisos() : null;
	}
}
