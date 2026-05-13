package local.alejandrogb.metricsserversdesktop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Envuelve las respuestas paginadas de api-py.
 * Estructura: { data, page, size, total, totalPages, hasNext }
 *
 * Actualmente solo se usa internamente en ApiClient.getDataPage() para
 * extraer el array "data". Queda disponible para cuando se implemente
 * paginación real en la UI.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse<T> {

	private List<T> data;
	private int page;
	private int size;
	private int total;
	private int totalPages;
	private boolean hasNext;

	public PageResponse() {
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public boolean isHasNext() {
		return hasNext;
	}

	public void setHasNext(boolean hasNext) {
		this.hasNext = hasNext;
	}

	@Override
	public String toString() {
		return "PageResponse{page=" + page + ", size=" + size + ", total=" + total
				+ ", totalPages=" + totalPages + ", hasNext=" + hasNext
				+ ", data.size=" + (data != null ? data.size() : 0) + '}';
	}
}
